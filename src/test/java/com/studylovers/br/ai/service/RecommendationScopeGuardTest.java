package com.studylovers.br.ai.service;

import com.studylovers.br.ai.dto.ChatMessageDTO;
import com.studylovers.br.ai.dto.CursoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationScopeGuardTest {

    private final CursoDTO javaCourse = new CursoDTO(
            1L, "Curso de Java Básico", "Fundamentos de Java", 40,
            "Iniciante", "Alura", "https://example.com/java", "Tecnologia");

    @Test
    void recusaPerguntasGeraisForaDoEscopoMesmoComHistoricoDeCurso() {
        assertFalse(RecommendationScopeGuard.isInScope(
                "Qual é a capital da França?",
                List.of(new ChatMessageDTO("assistant", "Recomendo o Curso de Java Básico.")),
                List.of(javaCourse)));
    }

    @Test
    void aceitaPedidoDeRecomendacaoDeCurso() {
        assertTrue(RecommendationScopeGuard.isInScope(
                "Qual curso devo fazer para aprender Java?", List.of(), List.of(javaCourse)));
    }

    @Test
    void aceitaContinuacaoCurtaDeUmaConversaSobreCursos() {
        assertTrue(RecommendationScopeGuard.isInScope(
                "E quanto tempo leva?",
                List.of(new ChatMessageDTO("user", "Qual curso de Java recomenda?")),
                List.of(javaCourse)));
    }
}
