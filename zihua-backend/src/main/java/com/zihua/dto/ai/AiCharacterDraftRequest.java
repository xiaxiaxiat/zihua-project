package com.zihua.dto.ai;

public class AiCharacterDraftRequest {

    private String code;
    private String character;
    private String pinyin;
    private String existingOrigin;
    private String existingMeaning;
    private String existingCulture;

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

    public String getExistingOrigin() {
        return existingOrigin;
    }

    public void setExistingOrigin(String existingOrigin) {
        this.existingOrigin = existingOrigin;
    }

    public String getExistingMeaning() {
        return existingMeaning;
    }

    public void setExistingMeaning(String existingMeaning) {
        this.existingMeaning = existingMeaning;
    }

    public String getExistingCulture() {
        return existingCulture;
    }

    public void setExistingCulture(String existingCulture) {
        this.existingCulture = existingCulture;
    }
}
