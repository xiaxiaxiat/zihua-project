package com.zihua.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zihua.dto.ai.AiCharacterDraftRequest;
import com.zihua.dto.ai.AiCharacterDraftResponse;
import com.zihua.dto.ai.AiStageDraftResponse;
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
public class AiCharacterDraftService {

    private static final Logger logger = LoggerFactory.getLogger(AiCharacterDraftService.class);

    private static final String MISSING_CONFIG_MESSAGE =
        "\u0041\u0049 \u670d\u52a1\u672a\u914d\u7f6e\uff0c\u8bf7\u8bbe\u7f6e AI_API_KEY";
    private static final String INVALID_URL_MESSAGE =
        "\u0041\u0049 \u670d\u52a1\u914d\u7f6e\u65e0\u6548\uff0c\u8bf7\u68c0\u67e5 AI_API_URL";
    private static final String UPSTREAM_ERROR_MESSAGE =
        "\u0041\u0049 \u670d\u52a1\u8c03\u7528\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5";
    private static final String PARSE_ERROR_MESSAGE =
        "\u0041\u0049 \u8fd4\u56de\u683c\u5f0f\u5f02\u5e38\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5";

    private static final String STAGE_NAME_JIAGUWEN = "\u7532\u9aa8\u6587";
    private static final String STAGE_NAME_JINWEN = "\u91d1\u6587";
    private static final String STAGE_NAME_XIAOZHUAN = "\u5c0f\u7bea";
    private static final String STAGE_NAME_LISHU = "\u96b6\u4e66";
    private static final String STAGE_NAME_KAISHU = "\u6977\u4e66";

    private static final List<StageTemplate> DEFAULT_STAGES = List.of(
        new StageTemplate("jiaguwen", STAGE_NAME_JIAGUWEN, 1, "jgw.png"),
        new StageTemplate("jinwen", STAGE_NAME_JINWEN, 2, "jw.png"),
        new StageTemplate("xiaozhuan", STAGE_NAME_XIAOZHUAN, 3, "xz.png"),
        new StageTemplate("lishu", STAGE_NAME_LISHU, 4, "ls.png"),
        new StageTemplate("kaishu", STAGE_NAME_KAISHU, 5, null)
    );

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String aiApiUrl;
    private final String aiApiKey;
    private final String aiApiModel;

    public AiCharacterDraftService(ObjectMapper objectMapper,
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

    public AiCharacterDraftResponse generateDraft(AiCharacterDraftRequest request) {
        validateRequest(request);
        ensureConfigured();

        String code = normalize(request.getCode());
        String prompt = buildPrompt(
            code,
            normalize(request.getCharacter()),
            trimToNull(request.getPinyin()),
            trimToNull(request.getExistingOrigin()),
            trimToNull(request.getExistingMeaning()),
            trimToNull(request.getExistingCulture())
        );

        String responseBody = callAiApi(prompt);
        String content = extractAssistantContent(responseBody);
        DraftPayload payload = parseDraftPayload(content);
        return buildResponse(code, payload);
    }

    private void validateRequest(AiCharacterDraftRequest request) {
        if (request == null) {
            throw new BadRequestException("\u8bf7\u6c42\u4f53\u4e0d\u80fd\u4e3a\u7a7a\u3002");
        }
        if (trimToNull(request.getCode()) == null) {
            throw new BadRequestException("Code is required.");
        }
        if (trimToNull(request.getCharacter()) == null) {
            throw new BadRequestException("Character is required.");
        }
    }

    private void ensureConfigured() {
        if (trimToNull(aiApiUrl) == null || trimToNull(aiApiKey) == null || trimToNull(aiApiModel) == null) {
            throw new ServiceUnavailableException(MISSING_CONFIG_MESSAGE);
        }
    }

    private String callAiApi(String prompt) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", aiApiModel);
            payload.put("temperature", 0.2);
            payload.put("max_tokens", 2500);
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

            logger.info("Calling AI draft service url={} model={}", aiApiUrl, aiApiModel);

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            logger.info("AI draft upstream status={}", response.statusCode());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warn("AI draft upstream non-2xx body={}", excerpt(response.body()));
                throw new BadGatewayException(UPSTREAM_ERROR_MESSAGE);
            }

            return response.body();
        } catch (IllegalArgumentException exception) {
            logger.warn("AI draft invalid config url={} message={}", aiApiUrl, exception.getMessage());
            throw new ServiceUnavailableException(INVALID_URL_MESSAGE);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logger.warn("AI draft interrupted type={} message={}",
                exception.getClass().getSimpleName(), exception.getMessage());
            throw new BadGatewayException(UPSTREAM_ERROR_MESSAGE);
        } catch (IOException exception) {
            logger.warn("AI draft I/O failure type={} message={}",
                exception.getClass().getSimpleName(), exception.getMessage());
            throw new BadGatewayException(UPSTREAM_ERROR_MESSAGE);
        }
    }

    private String extractAssistantContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                logger.warn("AI draft missing choices body={}", excerpt(responseBody));
                throw new BadGatewayException(UPSTREAM_ERROR_MESSAGE);
            }

            JsonNode contentNode = choices.get(0).path("message").path("content");
            String content = readMessageContent(contentNode);
            if (trimToNull(content) == null) {
                logger.warn("AI draft empty content body={}", excerpt(responseBody));
                throw new BadGatewayException(UPSTREAM_ERROR_MESSAGE);
            }
            return content;
        } catch (IOException exception) {
            logger.warn("AI draft response JSON parse failure type={} message={} body={}",
                exception.getClass().getSimpleName(), exception.getMessage(), excerpt(responseBody));
            throw new BadGatewayException(UPSTREAM_ERROR_MESSAGE);
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

    private DraftPayload parseDraftPayload(String rawContent) {
        String normalizedJson = extractJsonObject(stripMarkdownCodeFence(rawContent));
        try {
            JsonNode root = objectMapper.readTree(normalizedJson);
            if (!root.isObject()) {
                logger.warn("AI draft parsed root is not object content={}", excerpt(normalizedJson));
                throw new BadGatewayException(PARSE_ERROR_MESSAGE);
            }

            DraftPayload payload = new DraftPayload();
            payload.origin = trimToNull(root.path("origin").asText(null));
            payload.meaning = trimToNull(root.path("meaning").asText(null));
            payload.culture = trimToNull(root.path("culture").asText(null));
            payload.storyTitle = trimToNull(root.path("storyTitle").asText(null));
            payload.storyBody = trimToNull(root.path("storyBody").asText(null));
            payload.cardSummary = trimToNull(root.path("cardSummary").asText(null));
            payload.sealText = trimToNull(root.path("sealText").asText(null));
            payload.note = trimToNull(root.path("note").asText(null));
            payload.stages = parseStages(root.path("stages"));
            return payload;
        } catch (IOException exception) {
            logger.warn("AI draft content parse failure type={} message={} content={}",
                exception.getClass().getSimpleName(), exception.getMessage(), excerpt(normalizedJson));
            throw new BadGatewayException(PARSE_ERROR_MESSAGE);
        }
    }

    private List<DraftStagePayload> parseStages(JsonNode stagesNode) {
        if (!stagesNode.isArray()) {
            return List.of();
        }

        List<DraftStagePayload> stages = new ArrayList<>();
        for (JsonNode stageNode : stagesNode) {
            DraftStagePayload stage = new DraftStagePayload();
            stage.stageKey = trimToNull(stageNode.path("stageKey").asText(null));
            stage.description = trimToNull(stageNode.path("description").asText(null));
            stages.add(stage);
        }
        return stages;
    }

    private AiCharacterDraftResponse buildResponse(String code, DraftPayload payload) {
        AiCharacterDraftResponse response = new AiCharacterDraftResponse();
        response.setOrigin(payload.origin);
        response.setMeaning(payload.meaning);
        response.setCulture(payload.culture);
        response.setStoryTitle(payload.storyTitle);
        response.setStoryBody(payload.storyBody);
        response.setCardSummary(payload.cardSummary);
        response.setSealText(payload.sealText);
        response.setNote(payload.note);
        response.setStages(DEFAULT_STAGES.stream()
            .map(stage -> toStageResponse(stage, code, findDescription(payload.stages, stage.stageKey())))
            .toList());
        return response;
    }

    private AiStageDraftResponse toStageResponse(StageTemplate stageTemplate, String code, String description) {
        AiStageDraftResponse response = new AiStageDraftResponse();
        response.setStageKey(stageTemplate.stageKey());
        response.setStageName(stageTemplate.stageName());
        response.setSortOrder(stageTemplate.sortOrder());
        response.setImageUrl(stageTemplate.imageUrl(code));
        response.setDescription(description);
        return response;
    }

    private String findDescription(List<DraftStagePayload> stages, String stageKey) {
        for (DraftStagePayload stage : stages) {
            if (stageKey.equals(stage.stageKey) && stage.description != null) {
                return stage.description;
            }
        }
        return null;
    }

    private String buildSystemInstruction() {
        return """
            You are generating draft content for a Chinese character learning admin tool.
            Return only one valid JSON object.
            Do not return Markdown.
            Do not return code fences.
            Do not add explanation before or after the JSON object.
            All natural-language field values must be written in Simplified Chinese.
            Be cautious in tone.
            When uncertainty exists, use wording equivalent to:
            - common interpretation
            - can be understood as
            - still needs human review
            Do not claim you consulted any database, dictionary website, archive, or authority source.
            """;
    }

    private String buildPrompt(String code,
                               String character,
                               String pinyin,
                               String existingOrigin,
                               String existingMeaning,
                               String existingCulture) {
        return """
            Generate a draft JSON object for a Chinese character admin form.

            The JSON object must contain these top-level fields:
            - origin
            - meaning
            - culture
            - storyTitle
            - storyBody
            - cardSummary
            - sealText
            - note
            - stages

            General rules:
            - The JSON root must be an object.
            - All natural-language values must be in Simplified Chinese.
            - stages must contain exactly 5 objects.
            - Do not generate images.
            - imageUrl is only a suggested path; actual files will be prepared manually later.
            - Keep the tone careful and non-authoritative.

            Character input:
            - code: %s
            - character: %s
            - pinyin: %s
            - existingOrigin: %s
            - existingMeaning: %s
            - existingCulture: %s

            Writing preferences:
            - origin: 1-2 sentences
            - meaning: 2-4 sentences
            - culture: 2-4 sentences
            - storyTitle: a concise Chinese title
            - storyBody: a short child-friendly Chinese draft
            - cardSummary: 1-2 sentences
            - sealText: 2-6 Chinese characters
            - note: remind the admin what still needs human review
            - each stage description: 1-2 sentences

            Required stages:
            1. {"stageKey":"jiaguwen","stageName":"%s","imageUrl":"/character-images/%s/jgw.png","sortOrder":1}
            2. {"stageKey":"jinwen","stageName":"%s","imageUrl":"/character-images/%s/jw.png","sortOrder":2}
            3. {"stageKey":"xiaozhuan","stageName":"%s","imageUrl":"/character-images/%s/xz.png","sortOrder":3}
            4. {"stageKey":"lishu","stageName":"%s","imageUrl":"/character-images/%s/ls.png","sortOrder":4}
            5. {"stageKey":"kaishu","stageName":"%s","imageUrl":null,"sortOrder":5}

            stageKey, stageName, sortOrder, and imageUrl must exactly match the required values above.
            Only description should be newly written for each stage.
            """.formatted(
            code,
            character,
            orDefault(pinyin),
            orDefault(existingOrigin),
            orDefault(existingMeaning),
            orDefault(existingCulture),
            STAGE_NAME_JIAGUWEN,
            code,
            STAGE_NAME_JINWEN,
            code,
            STAGE_NAME_XIAOZHUAN,
            code,
            STAGE_NAME_LISHU,
            code,
            STAGE_NAME_KAISHU
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

    private record StageTemplate(String stageKey, String stageName, int sortOrder, String fileName) {
        private String imageUrl(String code) {
            if (fileName == null) {
                return null;
            }
            return "/character-images/" + code + "/" + fileName;
        }
    }

    private static class DraftPayload {
        private String origin;
        private String meaning;
        private String culture;
        private String storyTitle;
        private String storyBody;
        private String cardSummary;
        private String sealText;
        private String note;
        private List<DraftStagePayload> stages = List.of();
    }

    private static class DraftStagePayload {
        private String stageKey;
        private String description;
    }
}
