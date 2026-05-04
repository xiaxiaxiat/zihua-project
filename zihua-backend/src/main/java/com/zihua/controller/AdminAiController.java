package com.zihua.controller;

import com.zihua.dto.ai.AiCharacterDraftRequest;
import com.zihua.dto.ai.AiCharacterDraftResponse;
import com.zihua.service.AiCharacterDraftService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ai")
public class AdminAiController {

    private final AiCharacterDraftService aiCharacterDraftService;

    public AdminAiController(AiCharacterDraftService aiCharacterDraftService) {
        this.aiCharacterDraftService = aiCharacterDraftService;
    }

    @PostMapping("/character-draft")
    public AiCharacterDraftResponse generateCharacterDraft(@RequestBody AiCharacterDraftRequest request) {
        return aiCharacterDraftService.generateDraft(request);
    }
}
