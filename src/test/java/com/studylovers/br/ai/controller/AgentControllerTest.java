package com.studylovers.br.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylovers.br.BackendApplication;
import com.studylovers.br.ai.dto.AgentRequestDTO;
import com.studylovers.br.ai.dto.AgentResponseDTO;
import com.studylovers.br.ai.service.AgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgentService agentService;

    @Test
    void devePermitirHealthCheckSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/agent/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("studylovers-recommender"));
    }

    @Test
    void deveRejeitarRequisicaoSemChaveInterna() throws Exception {
        AgentRequestDTO request = new AgentRequestDTO(
                1L,
                UUID.randomUUID(),
                "Me recomende um curso",
                "Gabriel",
                List.of(),
                List.of()
        );

        mockMvc.perform(post("/api/agent/recomendar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveAceitarRequisicaoComChaveInternaValida() throws Exception {
        UUID conversationId = UUID.randomUUID();
        when(agentService.recomendar(any()))
                .thenReturn(new AgentResponseDTO("Recomendo o curso de Java.", conversationId));

        AgentRequestDTO request = new AgentRequestDTO(
                1L,
                conversationId,
                "Me recomende um curso",
                "Gabriel",
                List.of(),
                List.of()
        );

        mockMvc.perform(post("/api/agent/recomendar")
                        .header("X-Internal-Key", "studylovers-internal-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resposta").value("Recomendo o curso de Java."))
                .andExpect(jsonPath("$.conversationId").value(conversationId.toString()));
    }
}
