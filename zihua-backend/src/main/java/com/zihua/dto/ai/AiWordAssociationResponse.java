package com.zihua.dto.ai;

import java.util.List;

public class AiWordAssociationResponse {

    private String character;
    private List<AiWordItemResponse> words;

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public List<AiWordItemResponse> getWords() {
        return words;
    }

    public void setWords(List<AiWordItemResponse> words) {
        this.words = words;
    }
}
