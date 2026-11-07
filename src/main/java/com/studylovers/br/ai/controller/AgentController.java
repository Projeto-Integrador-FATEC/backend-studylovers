package com.studylovers.br.ai.controller;

import com.studylovers.br.ai.dto.AgentRequestDTO;
import com.studylovers.br.ai.dto.AgentResponseDTO;
import com.studylovers.br.ai.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping({"/recomendar", "/recomendacao"})
    public ResponseEntity<AgentResponseDTO> recomendar(
            @RequestBody AgentRequestDTO request,
            Authentication authentication) {
        AgentResponseDTO response = agentService.recomendar(request, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
