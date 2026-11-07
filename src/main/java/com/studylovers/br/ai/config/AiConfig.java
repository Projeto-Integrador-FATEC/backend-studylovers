package com.studylovers.br.ai.config;

import com.studylovers.br.ai.agent.RecomendacaoAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public RecomendacaoAgent recomendacaoAgent(
            @Value("${studylovers.ai.model:gemini-3.5-flash-lite}") String model) {
        return new RecomendacaoAgent(model);
    }
}
