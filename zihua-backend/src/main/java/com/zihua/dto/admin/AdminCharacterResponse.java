package com.zihua.dto.admin;

import java.util.List;

public class AdminCharacterResponse {

    private String code;
    private String character;
    private String pinyin;
    private String origin;
    private String meaning;
    private String culture;
    private String kaishuCharacter;
    private String storyTitle;
    private String storyBody;
    private boolean storyReviewed;
    private String cardSummary;
    private String sealText;
    private String note;
    private List<AdminStageResponse> stages;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public String getStoryBody() {
        return storyBody;
    }

    public void setStoryBody(String storyBody) {
        this.storyBody = storyBody;
    }

    public boolean isStoryReviewed() {
        return storyReviewed;
    }

    public void setStoryReviewed(boolean storyReviewed) {
        this.storyReviewed = storyReviewed;
    }

    public String getCardSummary() {
        return cardSummary;
    }

    public void setCardSummary(String cardSummary) {
        this.cardSummary = cardSummary;
    }

    public String getSealText() {
        return sealText;
    }

    public void setSealText(String sealText) {
        this.sealText = sealText;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<AdminStageResponse> getStages() {
        return stages;
    }

    public void setStages(List<AdminStageResponse> stages) {
        this.stages = stages;
    }
}
