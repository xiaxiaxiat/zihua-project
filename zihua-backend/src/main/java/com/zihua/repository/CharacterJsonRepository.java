package com.zihua.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zihua.model.CharacterRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Repository
public class CharacterJsonRepository {

    private final ObjectMapper objectMapper;

    public CharacterJsonRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<CharacterRecord> findAll() {
        ClassPathResource resource = new ClassPathResource("data/characters.json");

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<CharacterRecord>>() {
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load character data.", exception);
        }
    }
}
