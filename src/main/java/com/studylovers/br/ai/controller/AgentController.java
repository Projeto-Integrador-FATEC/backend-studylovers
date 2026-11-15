package com.studylovers.br.ai.controller;

import com.studylovers.br.ai.dto.AgentRequestDTO;
import com.studylovers.br.ai.dto.AgentResponseDTO;
import com.studylovers.br.ai.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "studylovers-recommender"));
    }

    @PostMapping({"/recomendar", "/recomendacao"})
    public ResponseEntity<AgentResponseDTO> recomendar(@RequestBody AgentRequestDTO request) {
        AgentResponseDTO response = agentService.recomendar(request);
        return ResponseEntity.ok(response);
    }
}
