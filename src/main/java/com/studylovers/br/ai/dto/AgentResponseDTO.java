package com.studylovers.br.ai.dto;

import java.util.UUID;

public record AgentResponseDTO(
        String resposta,
        UUID conversationId
) {}
