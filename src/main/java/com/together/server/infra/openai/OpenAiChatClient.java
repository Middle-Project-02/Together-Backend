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

/**
 * OpenAI GPT 모델과의 통신을 담당하는 클라이언트 컴포넌트입니다.
 * - 대화형 프롬프트 요청 및 스트리밍 응답 처리
 * - 단일 응답 처리
 */
@Component
public class OpenAiChatClient {

    private final WebClient openaiWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiChatClient(@Qualifier("openaiWebClient") WebClient openaiWebClient) {
        this.openaiWebClient = openaiWebClient;
    }

    /**
     * 요금제 조건 수집용 GPT 스트리밍 응답 메서드
     *
     * @param prompt 사용자 입력 프롬프트
     * @return Flux<String> 형태의 GPT 응답 스트림
     */
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

    /**
     * 멀티턴 대화 지원용 스트리밍 응답
     *
     * @param messages GPT에 보낼 이전 대화 목록 (role, content 포함)
     */
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

    /**
     * 대화 요약용 GPT 단일 응답 메서드
     *
     * @param prompt 사용자 입력 프롬프트
     * @return 요약 문자열
     */
    public String generateSummaryResponse(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        return openaiWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractFullText)
                .block();  // 블로킹 방식
    }

    /**
     * OpenAI 스트리밍 응답에서 텍스트 부분만 추출합니다.
     *
     * @param json JSON 형식의 응답 문자열
     * @return 추출된 텍스트 조각
     */
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

    /**
     * OpenAI 응답 JSON에서 전체 메시지를 추출합니다.
     *
     * @param json JSON 응답 문자열
     * @return 전체 응답 텍스트
     */
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
