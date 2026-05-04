package com.zihua.dto.admin;

import java.time.LocalDateTime;

public class AdminCharacterSummaryResponse {

    private String id;
    private String character;
    private String pinyin;
    private String origin;
    private boolean storyReviewed;
    private LocalDateTime updatedAt;

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

    public boolean isStoryReviewed() {
        return storyReviewed;
    }

    public void setStoryReviewed(boolean storyReviewed) {
        this.storyReviewed = storyReviewed;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
