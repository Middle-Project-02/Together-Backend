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
                Map.of("role", "system", "content",
                        String.join("\n",
                                "아래 문장에서 요금제 추천에 필요한 정보를 추출해서 JSON으로 만들어줘.",
                                "JSON 키: { \"voice\": \"\", \"data\": \"\", \"sms\": \"\", \"age\": \"\", \"type\": \"\" }",
                                "",
                                "- 통화(voice): 하루 또는 한 달 기준 통화 시간 (분)",
                                "- 데이터(data): 하루 또는 한 달 기준 유튜브/인터넷 사용 시간 (분 또는 시간)",
                                "- 문자(sms): 하루 또는 한 달 문자 개수",
                                "- 나이(age): 숫자 (예: 75)",
                                "- 통신망(type): LTE / 5G / 3G",
                                "",
                                "단위가 없으면 하루 기준으로 간주하고, 월 기준으로 환산하지 말고 그대로 값만 추출해줘.",
                                "없으면 null로 채워줘."
                        )
                ),
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
             //log.warn("JSON 파싱 실패", e);
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
        int value = digits.isEmpty() ? -1 : Integer.parseInt(digits);

        // 하루 단위 입력일 경우 → 한 달로 변환
        if (value > 0) {
            if (key.equals("voice") && lower.contains("하루")) {
                return String.valueOf(value * 30); // 하루 통화 → 월 통화(분)
            }
            if (key.equals("sms") && lower.contains("하루")) {
                return String.valueOf(value * 30); // 하루 문자 → 월 문자(건)
            }
            if (key.equals("data") && lower.contains("유튜브") || lower.contains("하루")) {
                int mbPerHour = 300; // 예: 유튜브 1시간 ≒ 300MB
                return String.valueOf(value * mbPerHour * 30); // 하루 n시간 → 월 MB
            }
        }

        // 일반 MB 단위 데이터
        if (key.equals("data") && !digits.isEmpty() && !lower.contains("하루")) {
            return String.valueOf(Integer.parseInt(digits) * 1000); // GB → MB
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
