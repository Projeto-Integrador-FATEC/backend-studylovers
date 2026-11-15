package com.studylovers.br.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import java.util.UUID;

public record AgentRequestDTO(
        Long userId,
        UUID conversationId,
        @JsonAlias("pergunta") String mensagem,
        String nomeUsuario,
        List<CursoDTO> catalogo,
        List<ChatMessageDTO> historico
) {}
