package com.studylovers.br.ai.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylovers.br.ai.agent.RecomendacaoAgent;
import com.studylovers.br.ai.dto.AgentRequestDTO;
import com.studylovers.br.ai.dto.AgentResponseDTO;
import com.studylovers.br.backend.model.Curso;
import com.studylovers.br.backend.model.User;
import com.studylovers.br.backend.repository.CursoRepository;
import com.studylovers.br.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
public class AgentService {

    private final RecomendacaoAgent recomendacaoAgent;
    private final UserRepository userRepository;
    private final CursoRepository cursoRepository;
    private final ObjectMapper objectMapper;

    public AgentService(
            RecomendacaoAgent recomendacaoAgent,
            UserRepository userRepository,
            CursoRepository cursoRepository,
            ObjectMapper objectMapper) {
        this.recomendacaoAgent = recomendacaoAgent;
        this.userRepository = userRepository;
        this.cursoRepository = cursoRepository;
        this.objectMapper = objectMapper;
    }

    public AgentResponseDTO recomendar(AgentRequestDTO request, String emailAutenticado) {
        if (request == null || !StringUtils.hasText(request.mensagem())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo 'mensagem' é obrigatório.");
        }

        if (!StringUtils.hasText(System.getenv("GOOGLE_API_KEY"))
                && !StringUtils.hasText(System.getenv("GEMINI_API_KEY"))) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "A variável GOOGLE_API_KEY não está configurada no ambiente do backend.");
        }

        User usuario = userRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (request.userId() != null && !request.userId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O userId informado não corresponde à conta autenticada.");
        }

        String catalogoJson = serializarCatalogo(cursoRepository.findAll());
        String nomeUsuario = StringUtils.hasText(usuario.getName()) ? usuario.getName() : "não informado";
        String idUsuarioAdk = usuario.getId().toString();
        String idSessaoAdk = UUID.randomUUID().toString();
        BaseAgent agente = recomendacaoAgent.criarComContexto(nomeUsuario, catalogoJson);
        InMemoryRunner runner = new InMemoryRunner(agente, "studylovers-recomendacoes");

        runner.sessionService().createSession(
                runner.appName(), idUsuarioAdk, Map.of(), idSessaoAdk).blockingGet();

        StringBuilder resposta = new StringBuilder();
        Content mensagem = Content.fromParts(Part.fromText(request.mensagem().trim()));
        runner.runAsync(idUsuarioAdk, idSessaoAdk, mensagem)
                .blockingForEach(event -> adicionarRespostaFinal(event, resposta));

        if (!StringUtils.hasText(resposta)) {
            throw new IllegalStateException("O agente não retornou uma resposta.");
        }

        return new AgentResponseDTO(resposta.toString().trim());
    }

    private String serializarCatalogo(List<Curso> cursos) {
        List<Map<String, Object>> catalogo = cursos.stream()
                .map(this::paraDadosDoCurso)
                .toList();
        try {
            return objectMapper.writeValueAsString(catalogo);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível preparar o catálogo de cursos.", exception);
        }
    }

    private Map<String, Object> paraDadosDoCurso(Curso curso) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("id", curso.getId());
        dados.put("nome", curso.getNome());
        dados.put("descricao", curso.getDescricao());
        dados.put("cargaHoraria", curso.getCargaHoraria());
        dados.put("nivel", curso.getNivel());
        dados.put("provedor", curso.getProvider());
        dados.put("url", curso.getUrl());
        dados.put("categoria", curso.getCategoria() == null ? null : curso.getCategoria().getNome());
        return dados;
    }

    private void adicionarRespostaFinal(Event event, StringBuilder resposta) {
        if (event.finalResponse()) {
            resposta.append(event.stringifyContent());
        }
    }
}
