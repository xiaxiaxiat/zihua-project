package com.zihua.dto.ai;

import java.util.List;

public class AiCharacterDraftResponse {

    private String origin;
    private String meaning;
    private String culture;
    private String storyTitle;
    private String storyBody;
    private String cardSummary;
    private String sealText;
    private String note;
    private List<AiStageDraftResponse> stages;

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

    public List<AiStageDraftResponse> getStages() {
        return stages;
    }

    public void setStages(List<AiStageDraftResponse> stages) {
        this.stages = stages;
    }
}
