package com.studylovers.br.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.studylovers.br.ai.agent.RecomendacaoAgent;
import com.studylovers.br.ai.dto.AgentRequestDTO;
import com.studylovers.br.ai.dto.AgentResponseDTO;
import com.studylovers.br.ai.dto.ChatMessageDTO;
import com.studylovers.br.ai.dto.CursoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentService {

    private final RecomendacaoAgent recomendacaoAgent;
    private final ObjectMapper objectMapper;

    public AgentService(
            RecomendacaoAgent recomendacaoAgent,
            ObjectMapper objectMapper) {
        this.recomendacaoAgent = recomendacaoAgent;
        this.objectMapper = objectMapper;
    }

    public AgentResponseDTO recomendar(AgentRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.mensagem())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo 'mensagem' é obrigatório.");
        }

        UUID conversationId = request.conversationId() == null ? UUID.randomUUID() : request.conversationId();
        String pergunta = request.mensagem().trim();
        List<ChatMessageDTO> historico = request.historico() != null ? request.historico() : Collections.emptyList();
        List<CursoDTO> catalogo = request.catalogo() != null ? request.catalogo() : Collections.emptyList();

        if (!RecommendationScopeGuard.isInScope(pergunta, historico, catalogo)) {
            return new AgentResponseDTO(RecommendationScopeGuard.OUT_OF_SCOPE_REPLY, conversationId);
        }

        String respostaDireta = responderComparacaoDeCarga(pergunta, historico, catalogo);
        if (respostaDireta != null) {
            return new AgentResponseDTO(respostaDireta, conversationId);
        }

        if (!hasApiKey()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "A variável GOOGLE_API_KEY não está configurada no ambiente do backend.");
        }

        String nomeUsuario = StringUtils.hasText(request.nomeUsuario()) ? request.nomeUsuario() : "Aluno";
        String catalogoJson = serializarCatalogo(catalogo);
        String historicoJson = serializarHistorico(historico);

        BaseAgent agente = recomendacaoAgent.criarComContexto(nomeUsuario, catalogoJson, historicoJson);

        String idUsuarioAdk = request.userId() != null ? request.userId().toString() : "user-" + UUID.randomUUID();
        String idSessaoAdk = conversationId.toString();

        InMemoryRunner runner = new InMemoryRunner(agente, "studylovers-recomendacoes");
        runner.sessionService().createSession(
                runner.appName(), idUsuarioAdk, Map.of(), idSessaoAdk).blockingGet();

        StringBuilder respostaGerada = new StringBuilder();
        Content mensagem = Content.fromParts(Part.fromText(pergunta));
        runner.runAsync(idUsuarioAdk, idSessaoAdk, mensagem)
                .blockingForEach(event -> adicionarRespostaFinal(event, respostaGerada));

        if (!StringUtils.hasText(respostaGerada)) {
            throw new IllegalStateException("O agente não retornou uma resposta.");
        }
        String resposta = respostaGerada.toString().trim();

        return new AgentResponseDTO(resposta, conversationId);
    }

    private boolean hasApiKey() {
        return StringUtils.hasText(System.getenv("GOOGLE_API_KEY"))
                || StringUtils.hasText(System.getenv("GEMINI_API_KEY"))
                || StringUtils.hasText(System.getProperty("GOOGLE_API_KEY"))
                || StringUtils.hasText(System.getProperty("GEMINI_API_KEY"));
    }

    private String responderComparacaoDeCarga(String pergunta, List<ChatMessageDTO> historico, List<CursoDTO> catalogo) {
        String perguntaNormalizada = normalizar(pergunta);
        boolean mencionaCarga = perguntaNormalizada.matches(".*\\b(hora|horas|carga)\\b.*");
        boolean pedeMenor = perguntaNormalizada.matches(".*\\b(menor|menos)\\b.*");
        boolean pedeMaior = perguntaNormalizada.matches(".*\\b(maior|mais)\\b.*");

        String contexto = (historico == null ? "" : historico.stream()
                .map(item -> item.content() != null ? normalizar(item.content()) : "")
                .reduce("", (a, b) -> a + " " + b));
        boolean continuacaoDeCarga = contexto.matches("(?s).*\\b(hora|horas|carga horaria)\\b.*");
        boolean continuacaoComparativa = continuacaoDeCarga
                && perguntaNormalizada.matches("(?s).*(\\bo com mais\\b|\\bo com menos\\b|\\bo maior\\b|\\bo menor\\b|\\ba maior\\b|\\ba menor\\b|\\bdeles.*\\b(mais|menos|maior|menor)\\b).*")
                && !perguntaNormalizada.matches("(?s).*\\b(barato|barata|caro|cara|preco|valor)\\b.*");
        boolean perguntaPorMenor = pedeMenor && (mencionaCarga || continuacaoComparativa);
        boolean perguntaPorMaior = pedeMaior && (mencionaCarga || continuacaoComparativa);
        if (!perguntaPorMenor && !perguntaPorMaior) {
            return null;
        }

        if (catalogo == null || catalogo.isEmpty()) {
            return "Não há cursos cadastrados no catálogo para comparar a carga horária.";
        }

        CursoDTO curso = catalogo.stream()
                .filter(c -> c.cargaHoraria() != null)
                .min((a, b) -> perguntaPorMenor
                        ? Integer.compare(a.cargaHoraria(), b.cargaHoraria())
                        : Integer.compare(b.cargaHoraria(), a.cargaHoraria()))
                .orElse(null);

        if (curso == null) {
            return "Não há cursos cadastrados no catálogo para comparar a carga horária.";
        }

        return "O curso com " + (perguntaPorMenor ? "menor" : "maior")
                + " carga horária disponível no catálogo é **" + curso.nome() + "** ("
                + curso.cargaHoraria() + " horas). Provedor: " + curso.provider()
                + "; nível: " + curso.nivel() + "; link: " + curso.url() + ".";
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private String serializarCatalogo(List<CursoDTO> cursos) {
        if (cursos == null || cursos.isEmpty()) {
            return "[]";
        }
        List<Map<String, Object>> catalogo = cursos.stream()
                .map(this::paraDadosDoCurso)
                .toList();
        try {
            return objectMapper.writeValueAsString(catalogo);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível preparar o catálogo de cursos.", exception);
        }
    }

    private Map<String, Object> paraDadosDoCurso(CursoDTO curso) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("id", curso.id());
        dados.put("nome", curso.nome());
        dados.put("descricao", curso.descricao());
        dados.put("cargaHoraria", curso.cargaHoraria());
        dados.put("nivel", curso.nivel());
        dados.put("provedor", curso.provider());
        dados.put("url", curso.url());
        dados.put("categoria", curso.categoria());
        return dados;
    }

    private String serializarHistorico(List<ChatMessageDTO> historico) {
        if (historico == null || historico.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(historico);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível salvar o histórico da conversa.", exception);
        }
    }

    private void adicionarRespostaFinal(Event event, StringBuilder resposta) {
        if (event.finalResponse()) {
            resposta.append(event.stringifyContent());
        }
    }
}
