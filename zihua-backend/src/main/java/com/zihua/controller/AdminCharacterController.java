package com.zihua.controller;

import com.zihua.dto.admin.AdminCharacterRequest;
import com.zihua.dto.admin.AdminCharacterResponse;
import com.zihua.dto.admin.AdminCharacterSummaryResponse;
import com.zihua.service.AdminCharacterService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/characters")
public class AdminCharacterController {

    private final AdminCharacterService adminCharacterService;

    public AdminCharacterController(AdminCharacterService adminCharacterService) {
        this.adminCharacterService = adminCharacterService;
    }

    @GetMapping
    public List<AdminCharacterSummaryResponse> getCharacters() {
        return adminCharacterService.getCharacters();
    }

    @GetMapping("/{code}")
    public AdminCharacterResponse getCharacter(@PathVariable String code) {
        return adminCharacterService.getCharacter(code);
    }

    @PostMapping
    public AdminCharacterResponse createCharacter(@RequestBody AdminCharacterRequest request) {
        return adminCharacterService.createCharacter(request);
    }

    @PutMapping("/{code}")
    public AdminCharacterResponse updateCharacter(@PathVariable String code,
                                                  @RequestBody AdminCharacterRequest request) {
        return adminCharacterService.updateCharacter(code, request);
    }

    @DeleteMapping("/{code}")
    public Map<String, Object> deleteCharacter(@PathVariable String code) {
        adminCharacterService.deleteCharacter(code);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "deleted");
        response.put("code", code);
        return response;
    }
}
