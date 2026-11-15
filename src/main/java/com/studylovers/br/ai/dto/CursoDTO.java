package com.studylovers.br.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CursoDTO(
        Long id,
        String nome,
        String descricao,
        Integer cargaHoraria,
        String nivel,
        @JsonAlias({"provedor", "provider"}) String provider,
        String url,
        String categoria
) {}

