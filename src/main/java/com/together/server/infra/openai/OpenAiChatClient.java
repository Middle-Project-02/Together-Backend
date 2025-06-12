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

    /**
     * 생성자 - OpenAI와 통신할 WebClient를 주입받습니다.
     *
     * @param openaiWebClient "openaiWebClient"로 지정된 WebClient Bean
     */
    public OpenAiChatClient(@Qualifier("openaiWebClient") WebClient openaiWebClient) {
        this.openaiWebClient = openaiWebClient;
    }

    /**
     * OpenAI GPT 모델에 메시지를 보내고 스트리밍 방식으로 응답을 수신합니다.
     *
     * @param prompt 사용자 입력 프롬프트
     * @return Flux<String> 형태의 GPT 응답 스트림
     */
    public Flux<String> streamChatCompletion(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("stream", true);

        return openaiWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractStreamText)
                .filter(chunk -> chunk != null && !chunk.isEmpty());
    }

    /**
     * OpenAI 스트리밍 응답에서 텍스트 부분만 추출합니다.
     *
     * @param json JSON 형식의 응답 문자열
     * @return 추출된 텍스트 조각, 없으면 빈 문자열
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
     * OpenAI GPT 모델에 메시지를 보내고 단일 응답을 받습니다.
     *
     * @param prompt 사용자 입력 프롬프트
     * @return 단일 응답 문자열 (전체 메시지)
     */
    public String getChatCompletion(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        return openaiWebClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractFullText)
                .block();  // 동기 블로킹 방식
    }

    /**
     * OpenAI 응답 JSON에서 전체 메시지를 추출합니다.
     *
     * @param json JSON 형식의 응답 문자열
     * @return 추출된 응답 텍스트, 실패 시 에러 메시지
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
