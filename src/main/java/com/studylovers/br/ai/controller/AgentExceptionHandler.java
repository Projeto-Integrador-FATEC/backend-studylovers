package com.studylovers.br.ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;

@RestControllerAdvice(assignableTypes = AgentController.class)
public class AgentExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentExceptionHandler.class);
    private static final String RATE_LIMIT_MESSAGE = "Limite de uso da IA atingido, tente novamente mais tarde";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handle(ResponseStatusException exception) {
        if (exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("erro", RATE_LIMIT_MESSAGE));
        }

        String message = exception.getReason() == null
                ? "Não foi possível processar a solicitação."
                : exception.getReason();

        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("erro", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception exception) {
        if (isRateLimit(exception)) {
            logger.warn("Limite de uso da API de IA atingido.", exception);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("erro", RATE_LIMIT_MESSAGE));
        }

        logger.error("Falha ao processar recomendação de cursos com IA.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "erro", "Falha ao consultar a IA"));
    }

    private boolean isRateLimit(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage();
            if (message != null) {
                String normalized = message.toUpperCase(Locale.ROOT);
                if (normalized.contains("RESOURCE_EXHAUSTED")
                        || normalized.matches("(?s).*\\b429\\b.*")
                        || normalized.contains("TOO MANY REQUESTS")) {
                    return true;
                }
            }
        }
        return false;
    }
}
