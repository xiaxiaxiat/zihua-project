package com.zihua.repository;

import com.zihua.entity.HanziCharacter;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HanziCharacterRepository extends JpaRepository<HanziCharacter, Long> {

    List<HanziCharacter> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "stages")
    Optional<HanziCharacter> findByCode(String code);
}
