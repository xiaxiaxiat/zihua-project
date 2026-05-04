package com.zihua.controller;

import com.zihua.dto.ai.AiWordAssociationRequest;
import com.zihua.dto.ai.AiWordAssociationResponse;
import com.zihua.service.AiWordAssociationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiWordAssociationController {

    private final AiWordAssociationService aiWordAssociationService;

    public AiWordAssociationController(AiWordAssociationService aiWordAssociationService) {
        this.aiWordAssociationService = aiWordAssociationService;
    }

    @PostMapping("/word-association")
    public AiWordAssociationResponse generateWordAssociation(@RequestBody AiWordAssociationRequest request) {
        return aiWordAssociationService.generateWordAssociation(request);
    }
}
