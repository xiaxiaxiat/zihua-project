package com.zihua.service;

import com.zihua.entity.HanziCharacter;
import com.zihua.entity.HanziStage;
import com.zihua.exception.NotFoundException;
import com.zihua.model.CardMeta;
import com.zihua.model.CharacterRecord;
import com.zihua.model.EvolutionStage;
import com.zihua.model.StoryContent;
import com.zihua.repository.HanziCharacterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CharacterService {

    private final HanziCharacterRepository hanziCharacterRepository;

    public CharacterService(HanziCharacterRepository hanziCharacterRepository) {
        this.hanziCharacterRepository = hanziCharacterRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCharacters() {
        return hanziCharacterRepository.findAllByOrderByIdAsc().stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public CharacterRecord getCharacterById(String id) {
        HanziCharacter character = hanziCharacterRepository.findByCode(id)
            .orElseThrow(() -> new NotFoundException("Character not found: " + id));
        return toRecord(character);
    }

    private Map<String, Object> toSummary(HanziCharacter entity) {
        Map<String, Object> summary = new LinkedHashMap<>();
        Map<String, Object> cardMeta = new LinkedHashMap<>();

        cardMeta.put("summary", entity.getCardSummary());
        summary.put("id", entity.getCode());
        summary.put("character", entity.getCharacterText());
        summary.put("pinyin", entity.getPinyin());
        summary.put("origin", entity.getOrigin());
        summary.put("culture", entity.getCulture());
        summary.put("cardMeta", cardMeta);

        return summary;
    }

    private CharacterRecord toRecord(HanziCharacter entity) {
        CharacterRecord record = new CharacterRecord();
        record.setId(entity.getCode());
        record.setCharacter(entity.getCharacterText());
        record.setPinyin(entity.getPinyin());
        record.setOrigin(entity.getOrigin());
        record.setMeaning(entity.getMeaning());
        record.setCulture(entity.getCulture());
        record.setKaishuCharacter(entity.getKaishuCharacter());
        record.setNote(entity.getNote());
        record.setCardMeta(toCardMeta(entity));
        record.setStory(toStory(entity));
        record.setStages(entity.getStages().stream()
            .map(this::toStage)
            .toList());
        return record;
    }

    private CardMeta toCardMeta(HanziCharacter entity) {
        CardMeta cardMeta = new CardMeta();
        cardMeta.setSummary(entity.getCardSummary());
        cardMeta.setSealText(entity.getSealText());
        return cardMeta;
    }

    private StoryContent toStory(HanziCharacter entity) {
        StoryContent story = new StoryContent();
        story.setTitle(entity.getStoryTitle());
        story.setBody(entity.getStoryBody());
        story.setReviewed(entity.isStoryReviewed());
        return story;
    }

    private EvolutionStage toStage(HanziStage entity) {
        EvolutionStage stage = new EvolutionStage();
        stage.setKey(entity.getStageKey());
        stage.setName(entity.getStageName());
        stage.setImageUrl(entity.getImageUrl());
        stage.setDescription(entity.getDescription());
        return stage;
    }
}
