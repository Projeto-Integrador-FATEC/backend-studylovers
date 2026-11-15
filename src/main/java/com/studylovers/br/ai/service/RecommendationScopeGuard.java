package com.studylovers.br.ai.service;

import com.studylovers.br.ai.dto.ChatMessageDTO;
import com.studylovers.br.ai.dto.CursoDTO;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.regex.Pattern;

final class RecommendationScopeGuard {

    static final String OUT_OF_SCOPE_REPLY = "Como assistente, estou aqui para recomendar cursos. "
            + "Se estiver buscando por cursos para iniciar seus estudos ou desenvolver novas habilidades, "
            + "estou à disposição para ajudar com recomendações do nosso catálogo!";

    private static final Pattern COURSE_INTENT = Pattern.compile(
            "\\b(cursos?|catalogo|trilhas?|formacao|treinamento|capacitacao|certificacao)\\b");
    private static final Pattern LEARNING_OR_CAREER_INTENT = Pattern.compile(
            "\\b(estud\\w*|aprend\\w*|habilidad\\w*|carreir\\w*|profiss\\w*|desenvolv\\w*|especializ\\w*|atuar\\w*|trabalhar\\w*|tornar\\w*)\\b");
    private static final Pattern CLEARLY_UNRELATED = Pattern.compile(
            "\\b(capital da|previsao do tempo|clima|receita culinaria|faca uma piada|resultado do jogo|noticias de politica)\\b");
    private static final Pattern CONTINUATION_REFERENCE = Pattern.compile(
            "\\b(esse|essa|isso|ele|ela|deles|delas|qual deles|qual delas|e quanto|quanto|mais|menos|preco|valor|link|nivel|horas|carga)\\b");
    private static final Pattern COURSE_CONTEXT = Pattern.compile(
            "\\b(cursos?|catalogo|estud\\w*|aprend\\w*|trilhas?|carreir\\w*|habilidad\\w*|formacao)\\b");

    private RecommendationScopeGuard() {}

    static boolean isInScope(String question, List<ChatMessageDTO> history, List<CursoDTO> catalog) {
        String normalizedQuestion = normalize(question);

        if (CLEARLY_UNRELATED.matcher(normalizedQuestion).find()) {
            return false;
        }

        if (COURSE_INTENT.matcher(normalizedQuestion).find()
                || LEARNING_OR_CAREER_INTENT.matcher(normalizedQuestion).find()
                || mentionsCatalogEntry(normalizedQuestion, catalog)) {
            return true;
        }

        return isCourseFollowUp(normalizedQuestion, history);
    }

    private static boolean isCourseFollowUp(String question, List<ChatMessageDTO> history) {
        if (question.length() > 100 || !CONTINUATION_REFERENCE.matcher(question).find() || history == null) {
            return false;
        }

        return history.stream()
                .filter(message -> message != null && message.content() != null)
                .map(message -> normalize(message.content()))
                .anyMatch(content -> COURSE_CONTEXT.matcher(content).find());
    }

    private static boolean mentionsCatalogEntry(String question, List<CursoDTO> catalog) {
        if (catalog == null) {
            return false;
        }

        return catalog.stream()
                .filter(course -> course != null)
                .flatMap(course -> Stream.of(course.nome(), course.provider(), course.categoria()))
                .filter(value -> value != null && value.trim().length() >= 4)
                .map(RecommendationScopeGuard::normalize)
                .anyMatch(question::contains);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
