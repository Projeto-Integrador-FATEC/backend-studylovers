package com.studylovers.br.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record AgentRequestDTO(
        Long userId,
        @JsonAlias("pergunta") String mensagem) {
}
