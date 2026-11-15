package com.studylovers.br.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylovers.br.ai.agent.RecomendacaoAgent;
import com.studylovers.br.ai.dto.AgentRequestDTO;
import com.studylovers.br.ai.dto.AgentResponseDTO;
import com.studylovers.br.ai.dto.ChatMessageDTO;
import com.studylovers.br.ai.dto.CursoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AgentServiceTest {

    private AgentService agentService;
    private RecomendacaoAgent recomendacaoAgent;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        recomendacaoAgent = new RecomendacaoAgent("gemini-3.5-flash-lite");
        objectMapper = new ObjectMapper();
        agentService = new AgentService(recomendacaoAgent, objectMapper);
    }

    @Test
    void deveLancarExcecaoQuandoMensagemEstiverVazia() {
        AgentRequestDTO request = new AgentRequestDTO(1L, UUID.randomUUID(), "", "Aluno", List.of(), List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> agentService.recomendar(request)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("mensagem"));
    }

    @Test
    void deveRecusarPerguntaForaDoEscopoSemChamarGemini() {
        AgentRequestDTO request = new AgentRequestDTO(
                1L,
                UUID.randomUUID(),
                "Qual é a capital da França?",
                "Gabriel",
                List.of(),
                List.of()
        );

        AgentResponseDTO response = agentService.recomendar(request);

        assertEquals(RecommendationScopeGuard.OUT_OF_SCOPE_REPLY, response.resposta());
        assertNotNull(response.conversationId());
    }

    @Test
    void deveResponderDiretamenteComparacaoDeMenorCargaHoraria() {
        CursoDTO cursoCurto = new CursoDTO(1L, "Curso Rápido", "Intro", 10, "Iniciante", "Alura", "http://alura.com/rapido", "Tech");
        CursoDTO cursoLongo = new CursoDTO(2L, "Curso Longo", "Completo", 80, "Avançado", "Udemy", "http://udemy.com/longo", "Tech");

        AgentRequestDTO request = new AgentRequestDTO(
                1L,
                UUID.randomUUID(),
                "Qual é o curso com menor carga horária?",
                "Gabriel",
                List.of(cursoLongo, cursoCurto),
                List.of()
        );

        AgentResponseDTO response = agentService.recomendar(request);

        assertNotNull(response);
        assertNotNull(response.resposta());
        assertTrue(response.resposta().contains("Curso Rápido"));
        assertTrue(response.resposta().contains("10 horas"));
        assertTrue(response.resposta().contains("menor"));
    }

    @Test
    void deveResponderDiretamenteComparacaoDeMaiorCargaHoraria() {
        CursoDTO cursoCurto = new CursoDTO(1L, "Curso Rápido", "Intro", 10, "Iniciante", "Alura", "http://alura.com/rapido", "Tech");
        CursoDTO cursoLongo = new CursoDTO(2L, "Curso Longo", "Completo", 80, "Avançado", "Udemy", "http://udemy.com/longo", "Tech");

        AgentRequestDTO request = new AgentRequestDTO(
                1L,
                UUID.randomUUID(),
                "Qual tem mais horas?",
                "Gabriel",
                List.of(cursoCurto, cursoLongo),
                List.of(new ChatMessageDTO("assistant", "Temos cursos com carga horaria variada."))
        );

        AgentResponseDTO response = agentService.recomendar(request);

        assertNotNull(response);
        assertNotNull(response.resposta());
        assertTrue(response.resposta().contains("Curso Longo"));
        assertTrue(response.resposta().contains("80 horas"));
        assertTrue(response.resposta().contains("maior"));
    }

    @Test
    void deveInformarQuandoCatalogoEstiverVazioParaComparacao() {
        AgentRequestDTO request = new AgentRequestDTO(
                1L,
                UUID.randomUUID(),
                "Qual é o curso de menor carga?",
                "Gabriel",
                List.of(),
                List.of()
        );

        AgentResponseDTO response = agentService.recomendar(request);

        assertNotNull(response);
        assertTrue(response.resposta().contains("Não há cursos cadastrados no catálogo"));
    }
}
