package com.zihua.model;

import java.util.List;

public class CharacterRecord {

    private String id;
    private String character;
    private String pinyin;
    private String origin;
    private String meaning;
    private String culture;
    private String kaishuCharacter;
    private List<EvolutionStage> stages;
    private StoryContent story;
    private CardMeta cardMeta;
    private String note;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture;
    }

    public String getKaishuCharacter() {
        return kaishuCharacter;
    }

    public void setKaishuCharacter(String kaishuCharacter) {
        this.kaishuCharacter = kaishuCharacter;
    }

    public List<EvolutionStage> getStages() {
        return stages;
    }

    public void setStages(List<EvolutionStage> stages) {
        this.stages = stages;
    }

    public StoryContent getStory() {
        return story;
    }

    public void setStory(StoryContent story) {
        this.story = story;
    }

    public CardMeta getCardMeta() {
        return cardMeta;
    }

    public void setCardMeta(CardMeta cardMeta) {
        this.cardMeta = cardMeta;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
