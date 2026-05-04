package com.zihua.service;

import com.zihua.dto.admin.AdminCharacterRequest;
import com.zihua.dto.admin.AdminCharacterResponse;
import com.zihua.dto.admin.AdminCharacterSummaryResponse;
import com.zihua.dto.admin.AdminStageRequest;
import com.zihua.dto.admin.AdminStageResponse;
import com.zihua.entity.HanziCharacter;
import com.zihua.entity.HanziStage;
import com.zihua.exception.BadRequestException;
import com.zihua.exception.NotFoundException;
import com.zihua.repository.HanziCharacterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AdminCharacterService {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");

    private static final List<StageTemplate> DEFAULT_STAGES = List.of(
        new StageTemplate("jiaguwen", "甲骨文", 1),
        new StageTemplate("jinwen", "金文", 2),
        new StageTemplate("xiaozhuan", "小篆", 3),
        new StageTemplate("lishu", "隶书", 4),
        new StageTemplate("kaishu", "楷书", 5)
    );

    private final HanziCharacterRepository hanziCharacterRepository;

    public AdminCharacterService(HanziCharacterRepository hanziCharacterRepository) {
        this.hanziCharacterRepository = hanziCharacterRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCharacterSummaryResponse> getCharacters() {
        return hanziCharacterRepository.findAllByOrderByUpdatedAtDescIdAsc().stream()
            .map(this::toSummaryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AdminCharacterResponse getCharacter(String code) {
        return toDetailResponse(findByCodeOrThrow(code));
    }

    @Transactional
    public AdminCharacterResponse createCharacter(AdminCharacterRequest request) {
        validateCreateRequest(request);

        HanziCharacter character = new HanziCharacter();
        character.setCode(normalize(request.getCode()));
        applyCharacterFields(character, request, false);
        rebuildStages(character, request.getStages());

        HanziCharacter saved = hanziCharacterRepository.save(character);
        return toDetailResponse(saved);
    }

    @Transactional
    public AdminCharacterResponse updateCharacter(String code, AdminCharacterRequest request) {
        HanziCharacter character = findByCodeOrThrow(code);
        validateUpdateRequest(code, request);

        applyCharacterFields(character, request, true);
        character.getStages().clear();
        hanziCharacterRepository.saveAndFlush(character);
        rebuildStages(character, request.getStages());

        HanziCharacter saved = hanziCharacterRepository.save(character);
        return toDetailResponse(saved);
    }

    @Transactional
    public void deleteCharacter(String code) {
        HanziCharacter character = findByCodeOrThrow(code);
        hanziCharacterRepository.delete(character);
    }

    private HanziCharacter findByCodeOrThrow(String code) {
        return hanziCharacterRepository.findByCode(code)
            .orElseThrow(() -> new NotFoundException("Character not found: " + code));
    }

    private void validateCreateRequest(AdminCharacterRequest request) {
        validateRequiredFields(request);

        String code = normalize(request.getCode());
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new BadRequestException("Code can only contain letters, numbers, and hyphens.");
        }
        if (hanziCharacterRepository.existsByCode(code)) {
            throw new BadRequestException("Character code already exists: " + code);
        }
    }

    private void validateUpdateRequest(String pathCode, AdminCharacterRequest request) {
        validateCharacterRequired(request.getCharacter());

        String requestCode = normalizeNullable(request.getCode());
        if (requestCode != null && !requestCode.equals(pathCode)) {
            throw new BadRequestException("Character code cannot be changed.");
        }
    }

    private void validateRequiredFields(AdminCharacterRequest request) {
        String code = normalizeNullable(request.getCode());
        if (code == null) {
            throw new BadRequestException("Code is required.");
        }
        validateCharacterRequired(request.getCharacter());
    }

    private void validateCharacterRequired(String character) {
        if (normalizeNullable(character) == null) {
            throw new BadRequestException("Character is required.");
        }
    }

    private void applyCharacterFields(HanziCharacter character, AdminCharacterRequest request, boolean keepCode) {
        if (!keepCode) {
            character.setCode(normalize(request.getCode()));
        }

        character.setCharacterText(normalize(request.getCharacter()));
        character.setPinyin(trimToNull(request.getPinyin()));
        character.setOrigin(trimToNull(request.getOrigin()));
        character.setMeaning(trimToNull(request.getMeaning()));
        character.setCulture(trimToNull(request.getCulture()));
        character.setKaishuCharacter(trimToNull(request.getKaishuCharacter()));
        character.setStoryTitle(trimToNull(request.getStoryTitle()));
        character.setStoryBody(trimToNull(request.getStoryBody()));
        character.setStoryReviewed(Boolean.TRUE.equals(request.getStoryReviewed()));
        character.setCardSummary(trimToNull(request.getCardSummary()));
        character.setSealText(trimToNull(request.getSealText()));
        character.setNote(trimToNull(request.getNote()));
    }

    private void rebuildStages(HanziCharacter character, List<AdminStageRequest> stageRequests) {
        List<AdminStageRequest> normalizedStages = normalizeStages(stageRequests);
        validateStages(normalizedStages);

        for (AdminStageRequest stageRequest : normalizedStages) {
            HanziStage stage = new HanziStage();
            stage.setStageKey(normalize(stageRequest.getStageKey()));
            stage.setStageName(normalize(stageRequest.getStageName()));
            stage.setImageUrl(trimToNull(stageRequest.getImageUrl()));
            stage.setDescription(trimToNull(stageRequest.getDescription()));
            stage.setSortOrder(stageRequest.getSortOrder());
            character.addStage(stage);
        }
    }

    private List<AdminStageRequest> normalizeStages(List<AdminStageRequest> stageRequests) {
        if (stageRequests == null || stageRequests.isEmpty()) {
            return DEFAULT_STAGES.stream()
                .map(template -> {
                    AdminStageRequest request = new AdminStageRequest();
                    request.setStageKey(template.stageKey());
                    request.setStageName(template.stageName());
                    request.setSortOrder(template.sortOrder());
                    request.setImageUrl(null);
                    request.setDescription(null);
                    return request;
                })
                .toList();
        }

        return stageRequests;
    }

    private void validateStages(List<AdminStageRequest> stageRequests) {
        if (stageRequests.size() != DEFAULT_STAGES.size()) {
            throw new BadRequestException("Exactly 5 stages are required.");
        }

        Set<String> stageKeys = new HashSet<>();
        Set<Integer> sortOrders = new HashSet<>();

        for (AdminStageRequest stage : stageRequests) {
            String stageKey = normalizeNullable(stage.getStageKey());
            String stageName = normalizeNullable(stage.getStageName());

            if (stageKey == null || stageName == null || stage.getSortOrder() == null) {
                throw new BadRequestException("Each stage requires stageKey, stageName, and sortOrder.");
            }
            if (!stageKeys.add(stageKey)) {
                throw new BadRequestException("Duplicate stageKey is not allowed: " + stageKey);
            }
            if (!sortOrders.add(stage.getSortOrder())) {
                throw new BadRequestException("Duplicate sortOrder is not allowed: " + stage.getSortOrder());
            }
        }
    }

    private AdminCharacterSummaryResponse toSummaryResponse(HanziCharacter character) {
        AdminCharacterSummaryResponse response = new AdminCharacterSummaryResponse();
        response.setId(character.getCode());
        response.setCharacter(character.getCharacterText());
        response.setPinyin(character.getPinyin());
        response.setOrigin(character.getOrigin());
        response.setStoryReviewed(character.isStoryReviewed());
        response.setUpdatedAt(character.getUpdatedAt());
        return response;
    }

    private AdminCharacterResponse toDetailResponse(HanziCharacter character) {
        AdminCharacterResponse response = new AdminCharacterResponse();
        response.setCode(character.getCode());
        response.setCharacter(character.getCharacterText());
        response.setPinyin(character.getPinyin());
        response.setOrigin(character.getOrigin());
        response.setMeaning(character.getMeaning());
        response.setCulture(character.getCulture());
        response.setKaishuCharacter(character.getKaishuCharacter());
        response.setStoryTitle(character.getStoryTitle());
        response.setStoryBody(character.getStoryBody());
        response.setStoryReviewed(character.isStoryReviewed());
        response.setCardSummary(character.getCardSummary());
        response.setSealText(character.getSealText());
        response.setNote(character.getNote());
        response.setStages(character.getStages().stream()
            .map(this::toStageResponse)
            .toList());
        return response;
    }

    private AdminStageResponse toStageResponse(HanziStage stage) {
        AdminStageResponse response = new AdminStageResponse();
        response.setStageKey(stage.getStageKey());
        response.setStageName(stage.getStageName());
        response.setImageUrl(stage.getImageUrl());
        response.setDescription(stage.getDescription());
        response.setSortOrder(stage.getSortOrder());
        return response;
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToNull(String value) {
        return normalizeNullable(value);
    }

    private record StageTemplate(String stageKey, String stageName, int sortOrder) {
    }
}
