package com.together.server.infra.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiChatClient {

    private final WebClient openaiWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiChatClient(@Qualifier("openaiWebClient") WebClient openaiWebClient) {
        this.openaiWebClient = openaiWebClient;
    }

    public Flux<String> streamChatCompletion(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("stream", true);
        body.put("temperature", 0.7);
        body.put("top_p", 0.9);
        body.put("frequency_penalty", 0.1);
        body.put("presence_penalty", 0.1);

        return openaiWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractStreamText)
                .filter(chunk -> chunk != null && !chunk.isEmpty());
    }

    public Flux<String> streamMultiturnChatCompletion(List<Map<String, String>> messages) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", messages);
        body.put("stream", true);
        body.put("temperature", 0.7);
        body.put("top_p", 0.9);
        body.put("frequency_penalty", 0.1);
        body.put("presence_penalty", 0.1);

        return openaiWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractStreamText)
                .filter(chunk -> chunk != null && !chunk.isEmpty());
    }

    public String generateSummaryResponse(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        return openaiWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractFullText)
                .block();
    }

    public String extractUserConditions(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(
                Map.of("role", "system", "content", "사용자의 문장에서 아래 항목들을 JSON 형태로 추출해줘. 없는 항목은 null로 채워줘. { \"voice\": \"\", \"data\": \"\", \"sms\": \"\", \"age\": \"\", \"type\": \"\" }"),
                Map.of("role", "user", "content", prompt)
        ));

        return openaiWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractFullText)
                .block();
    }

    public Map<String, String> parseConditionJson(String json) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode node = objectMapper.readTree(json);
            for (String key : List.of("voice", "data", "sms", "age", "type")) {
                JsonNode val = node.get(key);
                if (val != null && !val.isNull() && !val.asText().isBlank()) {
                    String cleaned = cleanNumericValue(val.asText(), key);
                    if (cleaned != null) {
                        result.put(key, cleaned);
                    }
                }
            }
        } catch (Exception e) {
             log.warn("JSON 파싱 실패", e);
        }
        return result;
    }

    private String cleanNumericValue(String raw, String key) {
        if (raw == null) return null;

        String lower = raw.toLowerCase();
        if (lower.contains("무제한")) {
            return "999999";
        }

        if (key.equals("type")) {
            if (lower.contains("lte")) return "3";
            if (lower.contains("5g")) return "6";
            if (lower.contains("3g")) return "2";
        }

        if (key.equals("age")) {
            if (lower.contains("청소년")) return "18";
            if (lower.contains("성인")) return "20";
            if (lower.contains("실버") || lower.contains("노인")) return "65";
        }

        String digits = raw.replaceAll("[^\\d]", "");

        if (key.equals("data") && !digits.isEmpty()) {
            int mb = Integer.parseInt(digits) * 1000;
            return String.valueOf(mb);
        }

        return digits.isEmpty() ? null : digits;
    }

    private String extractStreamText(String json) {
        try {
            if (json == null || json.isEmpty() || json.contains("[DONE]")) {
                return "";
            }
            JsonNode root = objectMapper.readTree(json);
            JsonNode contentNode = root.path("choices").get(0).path("delta").path("content");
            return contentNode.isMissingNode() ? "" : contentNode.asText();
        } catch (Exception e) {
            return "";
        }
    }

    private String extractFullText(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            return contentNode.isMissingNode() ? "AI 응답 파싱 오류" : contentNode.asText();
        } catch (Exception e) {
            return "AI 응답 파싱 오류: " + e.getMessage();
        }
    }
}
