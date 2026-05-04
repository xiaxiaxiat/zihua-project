package com.zihua.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zihua.dto.ai.AiWordAssociationRequest;
import com.zihua.dto.ai.AiWordAssociationResponse;
import com.zihua.dto.ai.AiWordItemResponse;
import com.zihua.exception.BadGatewayException;
import com.zihua.exception.BadRequestException;
import com.zihua.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiWordAssociationService {

    private static final Logger logger = LoggerFactory.getLogger(AiWordAssociationService.class);

    private static final String MESSAGE_MISSING_CHARACTER = "\u8bf7\u63d0\u4f9b\u6c49\u5b57";
    private static final String MESSAGE_MISSING_CONFIG =
        "\u0041\u0049 \u670d\u52a1\u672a\u914d\u7f6e\uff0c\u8bf7\u8bbe\u7f6e AI_API_KEY";
    private static final String MESSAGE_INVALID_URL =
        "\u0041\u0049 \u670d\u52a1\u914d\u7f6e\u65e0\u6548\uff0c\u8bf7\u68c0\u67e5 AI_API_URL";
    private static final String MESSAGE_AUTH_FAILURE =
        "\u0041\u0049 \u670d\u52a1\u8ba4\u8bc1\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5 API Key";
    private static final String MESSAGE_RATE_LIMIT =
        "\u0041\u0049 \u670d\u52a1\u8bf7\u6c42\u8fc7\u4e8e\u9891\u7e41\u6216\u989d\u5ea6\u4e0d\u8db3";
    private static final String MESSAGE_UPSTREAM_FAILURE =
        "\u0041\u0049 \u670d\u52a1\u8c03\u7528\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5";
    private static final String MESSAGE_PARSE_FAILURE =
        "\u0041\u0049 \u8fd4\u56de\u683c\u5f0f\u5f02\u5e38\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String aiApiUrl;
    private final String aiApiKey;
    private final String aiApiModel;

    public AiWordAssociationService(ObjectMapper objectMapper,
                                    @Value("${ai.api.url:}") String aiApiUrl,
                                    @Value("${ai.api.key:}") String aiApiKey,
                                    @Value("${ai.api.model:}") String aiApiModel) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.aiApiUrl = aiApiUrl;
        this.aiApiKey = aiApiKey;
        this.aiApiModel = aiApiModel;
    }

    public AiWordAssociationResponse generateWordAssociation(AiWordAssociationRequest request) {
        validateRequest(request);
        ensureConfigured();

        String character = normalize(request.getCharacter());
        String prompt = buildPrompt(request);

        String responseBody = callAiApi(prompt);
        String content = extractAssistantContent(responseBody);
        ParsedAssociation parsed = parseAssociation(content);
        return buildResponse(character, parsed);
    }

    private void validateRequest(AiWordAssociationRequest request) {
        if (request == null || trimToNull(request.getCharacter()) == null) {
            throw new BadRequestException(MESSAGE_MISSING_CHARACTER);
        }
    }

    private void ensureConfigured() {
        if (trimToNull(aiApiUrl) == null || trimToNull(aiApiKey) == null || trimToNull(aiApiModel) == null) {
            throw new ServiceUnavailableException(MESSAGE_MISSING_CONFIG);
        }
    }

    private String callAiApi(String prompt) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", aiApiModel);
            payload.put("temperature", 0.3);
            payload.put("max_tokens", 1800);
            payload.put("response_format", Map.of("type", "json_object"));
            payload.put("messages", List.of(
                Map.of("role", "system", "content", buildSystemInstruction()),
                Map.of("role", "user", "content", prompt)
            ));

            String requestJson = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(URI.create(aiApiUrl))
                .timeout(Duration.ofSeconds(45))
                .header("Authorization", "Bearer " + aiApiKey)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build();

            logger.info("Calling AI word association url={} model={}", aiApiUrl, aiApiModel);

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            logger.info("AI word association upstream status={}", response.statusCode());

            if (response.statusCode() == 401 || response.statusCode() == 403) {
                logger.warn("AI word association auth failure body={}", excerpt(response.body()));
                throw new BadGatewayException(MESSAGE_AUTH_FAILURE);
            }
            if (response.statusCode() == 429) {
                logger.warn("AI word association rate limited body={}", excerpt(response.body()));
                throw new BadGatewayException(MESSAGE_RATE_LIMIT);
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warn("AI word association non-2xx body={}", excerpt(response.body()));
                throw new BadGatewayException(MESSAGE_UPSTREAM_FAILURE);
            }

            return response.body();
        } catch (IllegalArgumentException exception) {
            logger.warn("AI word association invalid config url={} message={}", aiApiUrl, exception.getMessage());
            throw new ServiceUnavailableException(MESSAGE_INVALID_URL);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logger.warn("AI word association interrupted type={} message={}",
                exception.getClass().getSimpleName(), exception.getMessage());
            throw new BadGatewayException(MESSAGE_UPSTREAM_FAILURE);
        } catch (IOException exception) {
            logger.warn("AI word association I/O failure type={} message={}",
                exception.getClass().getSimpleName(), exception.getMessage());
            throw new BadGatewayException(MESSAGE_UPSTREAM_FAILURE);
        }
    }

    private String extractAssistantContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                logger.warn("AI word association missing choices body={}", excerpt(responseBody));
                throw new BadGatewayException(MESSAGE_UPSTREAM_FAILURE);
            }

            JsonNode contentNode = choices.get(0).path("message").path("content");
            String content = readMessageContent(contentNode);
            if (trimToNull(content) == null) {
                logger.warn("AI word association empty content body={}", excerpt(responseBody));
                throw new BadGatewayException(MESSAGE_UPSTREAM_FAILURE);
            }
            return content;
        } catch (IOException exception) {
            logger.warn("AI word association response parse failure type={} message={} body={}",
                exception.getClass().getSimpleName(), exception.getMessage(), excerpt(responseBody));
            throw new BadGatewayException(MESSAGE_UPSTREAM_FAILURE);
        }
    }

    private String readMessageContent(JsonNode contentNode) {
        if (contentNode == null || contentNode.isMissingNode() || contentNode.isNull()) {
            return null;
        }
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }
        if (contentNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : contentNode) {
                if (item.isTextual()) {
                    builder.append(item.asText());
                    continue;
                }

                JsonNode textNode = item.path("text");
                if (textNode.isTextual()) {
                    builder.append(textNode.asText());
                }
            }
            return builder.toString();
        }
        return contentNode.toString();
    }

    private ParsedAssociation parseAssociation(String rawContent) {
        String normalizedJson = extractJsonObject(stripMarkdownCodeFence(rawContent));
        try {
            JsonNode root = objectMapper.readTree(normalizedJson);
            if (!root.isObject()) {
                logger.warn("AI word association root is not object content={}", excerpt(normalizedJson));
                throw new BadGatewayException(MESSAGE_PARSE_FAILURE);
            }

            ParsedAssociation parsed = new ParsedAssociation();
            parsed.character = trimToNull(root.path("character").asText(null));
            parsed.words = parseWords(root.path("words"));
            if (parsed.words.size() < 1) {
                logger.warn("AI word association returned no words content={}", excerpt(normalizedJson));
                throw new BadGatewayException(MESSAGE_PARSE_FAILURE);
            }
            if (parsed.words.size() > 8) {
                parsed.words = parsed.words.subList(0, 8);
            }
            return parsed;
        } catch (IOException exception) {
            logger.warn("AI word association content parse failure type={} message={} content={}",
                exception.getClass().getSimpleName(), exception.getMessage(), excerpt(normalizedJson));
            throw new BadGatewayException(MESSAGE_PARSE_FAILURE);
        }
    }

    private List<ParsedWordItem> parseWords(JsonNode wordsNode) {
        if (!wordsNode.isArray()) {
            return List.of();
        }

        List<ParsedWordItem> words = new ArrayList<>();
        for (JsonNode node : wordsNode) {
            String word = trimToNull(node.path("word").asText(null));
            String meaning = trimToNull(node.path("meaning").asText(null));
            String usage = trimToNull(node.path("usage").asText(null));
            if (word == null || meaning == null || usage == null) {
                continue;
            }

            ParsedWordItem item = new ParsedWordItem();
            item.word = word;
            item.pinyin = trimToNull(node.path("pinyin").asText(null));
            item.meaning = meaning;
            item.usage = usage;
            item.example = trimToNull(node.path("example").asText(null));
            words.add(item);
        }
        return words;
    }

    private AiWordAssociationResponse buildResponse(String requestedCharacter, ParsedAssociation parsed) {
        AiWordAssociationResponse response = new AiWordAssociationResponse();
        response.setCharacter(parsed.character == null ? requestedCharacter : parsed.character);
        response.setWords(parsed.words.stream().map(this::toWordItemResponse).toList());
        return response;
    }

    private AiWordItemResponse toWordItemResponse(ParsedWordItem parsed) {
        AiWordItemResponse item = new AiWordItemResponse();
        item.setWord(parsed.word);
        item.setPinyin(parsed.pinyin);
        item.setMeaning(parsed.meaning);
        item.setUsage(parsed.usage);
        item.setExample(parsed.example);
        return item;
    }

    private String buildSystemInstruction() {
        return """
            You are generating Chinese word associations for a public-facing character detail page.
            Return only one valid JSON object.
            Do not return Markdown.
            Do not return code fences.
            Do not add explanations before or after the JSON object.
            All natural-language output must be written in Simplified Chinese.
            The audience is primary and middle school students plus general culture enthusiasts.
            Prefer clear, friendly, accessible explanations.
            Avoid obscure, vulgar, unsafe, or unsuitable words.
            Do not claim any authoritative citation or source lookup.
            Use careful wording for culture notes, such as common usage or can be understood as.
            """;
    }

    private String buildPrompt(AiWordAssociationRequest request) {
        return """
            Generate a JSON object for AI word association.

            Required JSON structure:
            {
              "character": "string",
              "words": [
                {
                  "word": "string",
                  "pinyin": "string",
                  "meaning": "string",
                  "usage": "string",
                  "example": "string"
                }
              ]
            }

            Rules:
            - The root must be a JSON object.
            - words must contain 5 to 8 items.
            - Every item must contain word, meaning, and usage.
            - pinyin and example may be present but can be short.
            - Word choices must be strongly related to the target character.
            - Prefer common words plus words with cultural imagery.
            - Keep language easy to understand.

            Input:
            - code: %s
            - character: %s
            - pinyin: %s
            - meaning: %s
            - culture: %s
            """.formatted(
            orDefault(trimToNull(request.getCode())),
            normalize(request.getCharacter()),
            orDefault(trimToNull(request.getPinyin())),
            orDefault(trimToNull(request.getMeaning())),
            orDefault(trimToNull(request.getCulture()))
        );
    }

    private String stripMarkdownCodeFence(String rawContent) {
        String trimmed = rawContent == null ? "" : rawContent.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int firstLineBreak = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineBreak < 0 || lastFence <= firstLineBreak) {
            return trimmed;
        }

        return trimmed.substring(firstLineBreak + 1, lastFence).trim();
    }

    private String extractJsonObject(String content) {
        String trimmed = content == null ? "" : content.trim();
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }

    private String excerpt(String value) {
        if (value == null) {
            return "";
        }
        String compact = value.replace('\r', ' ').replace('\n', ' ').trim();
        if (compact.length() <= 500) {
            return compact;
        }
        return compact.substring(0, 500);
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String orDefault(String value) {
        return value == null ? "none" : value;
    }

    private static class ParsedAssociation {
        private String character;
        private List<ParsedWordItem> words = List.of();
    }

    private static class ParsedWordItem {
        private String word;
        private String pinyin;
        private String meaning;
        private String usage;
        private String example;
    }
}
