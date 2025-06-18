package com.together.server.infra.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Bean(name = "openaiWebClient")
    public WebClient openaiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "smartChoiceWebClient")
    public WebClient smartChoiceWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.smartchoice.or.kr/api")
                .defaultHeader("Content-Type", "application/xml")
                .build();
    }
}