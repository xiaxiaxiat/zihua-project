package com.zihua.service;

import com.zihua.exception.NotFoundException;
import com.zihua.model.CharacterRecord;
import com.zihua.repository.CharacterJsonRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CharacterService {

    private final CharacterJsonRepository characterJsonRepository;

    public CharacterService(CharacterJsonRepository characterJsonRepository) {
        this.characterJsonRepository = characterJsonRepository;
    }

    public List<Map<String, Object>> getCharacters() {
        return characterJsonRepository.findAll().stream()
            .map(this::toSummary)
            .toList();
    }

    public CharacterRecord getCharacterById(String id) {
        return characterJsonRepository.findAll().stream()
            .filter(character -> character.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("未找到字库记录: " + id));
    }

    private Map<String, Object> toSummary(CharacterRecord characterRecord) {
        Map<String, Object> summary = new LinkedHashMap<>();
        Map<String, Object> cardMeta = new LinkedHashMap<>();

        cardMeta.put("summary", characterRecord.getCardMeta().getSummary());
        summary.put("id", characterRecord.getId());
        summary.put("character", characterRecord.getCharacter());
        summary.put("pinyin", characterRecord.getPinyin());
        summary.put("origin", characterRecord.getOrigin());
        summary.put("culture", characterRecord.getCulture());
        summary.put("cardMeta", cardMeta);

        return summary;
    }
}
