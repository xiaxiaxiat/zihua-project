package com.zihua.initializer;

import com.zihua.entity.HanziCharacter;
import com.zihua.entity.HanziStage;
import com.zihua.model.CardMeta;
import com.zihua.model.CharacterRecord;
import com.zihua.model.EvolutionStage;
import com.zihua.model.StoryContent;
import com.zihua.repository.CharacterJsonRepository;
import com.zihua.repository.HanziCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CharacterDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CharacterDataInitializer.class);

    private final HanziCharacterRepository hanziCharacterRepository;
    private final CharacterJsonRepository characterJsonRepository;

    public CharacterDataInitializer(HanziCharacterRepository hanziCharacterRepository,
                                    CharacterJsonRepository characterJsonRepository) {
        this.hanziCharacterRepository = hanziCharacterRepository;
        this.characterJsonRepository = characterJsonRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (hanziCharacterRepository.count() > 0) {
            log.info("hanzi_character already contains data. Skipping initialization.");
            return;
        }

        List<HanziCharacter> characters = characterJsonRepository.findAll().stream()
            .map(this::toEntity)
            .toList();

        hanziCharacterRepository.saveAll(characters);
        log.info("Initialized {} hanzi characters from characters.json.", characters.size());
    }

    private HanziCharacter toEntity(CharacterRecord record) {
        HanziCharacter character = new HanziCharacter();
        character.setCode(record.getId());
        character.setCharacterText(record.getCharacter());
        character.setPinyin(record.getPinyin());
        character.setOrigin(record.getOrigin());
        character.setMeaning(record.getMeaning());
        character.setCulture(record.getCulture());
        character.setKaishuCharacter(record.getKaishuCharacter());
        character.setNote(record.getNote());

        StoryContent story = record.getStory();
        if (story != null) {
            character.setStoryTitle(story.getTitle());
            character.setStoryBody(story.getBody());
            character.setStoryReviewed(story.isReviewed());
        }

        CardMeta cardMeta = record.getCardMeta();
        if (cardMeta != null) {
            character.setCardSummary(cardMeta.getSummary());
            character.setSealText(cardMeta.getSealText());
        }

        List<EvolutionStage> stages = record.getStages();
        if (stages != null) {
            for (int index = 0; index < stages.size(); index++) {
                character.addStage(toStageEntity(stages.get(index), index + 1));
            }
        }

        return character;
    }

    private HanziStage toStageEntity(EvolutionStage stage, int sortOrder) {
        HanziStage entity = new HanziStage();
        entity.setStageKey(stage.getKey());
        entity.setStageName(stage.getName());
        entity.setImageUrl(stage.getImageUrl());
        entity.setDescription(stage.getDescription());
        entity.setSortOrder(sortOrder);
        return entity;
    }
}
