package com.zihua.controller;

import com.zihua.model.CharacterRecord;
import com.zihua.service.CharacterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping("/api/characters")
    public List<Map<String, Object>> getCharacters() {
        return characterService.getCharacters();
    }

    @GetMapping("/api/characters/{id}")
    public CharacterRecord getCharacterById(@PathVariable String id) {
        return characterService.getCharacterById(id);
    }
}
