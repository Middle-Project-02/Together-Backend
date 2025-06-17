package com.together.server.infra.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class OpenAiChatClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiChatClient(WebClient openaiWebClient) {
        this.webClient = openaiWebClient;
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
        body.put("temperature", 0.7);
        body.put("top_p", 0.9);
        body.put("frequency_penalty", 0.1);
        body.put("presence_penalty", 0.1);

        return webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractStreamContent)
                .filter(chunk -> chunk != null && !chunk.isEmpty());
    }

    /**
     * 요금제 추천용 GPT 호출 (스트리밍)
     *
     * @param userInput 사용자 입력
     * @return Flux<String> 형태의 GPT 응답 스트림
     */
    public Flux<String> generatePlanStream(String userInput) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", userInput)));
        body.put("stream", true);

        return webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractStreamContent)
                .filter(chunk -> chunk != null && !chunk.isEmpty());
    }

    /**
     * 대화 요약용 GPT 호출 (싱글 호출)
     */
    public String generateSummaryResponse(String userInput) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", userInput)));

        return webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractContent)
                .block();
    }

    /**
     * 요금제 추천 Function Calling GPT 호출
     */
    public Mono<String> recommendPlanWithFunctionCalling(Map<String, String> userInfo, List<Map<String, String>> planList) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-3.5-turbo-0613");
            body.put("messages", List.of(Map.of("role", "user", "content", "사용자 정보와 요금제 리스트를 바탕으로 최적의 요금제를 추천해주세요.")));
            body.put("functions", List.of(objectMapper.readValue(FUNCTION_RECOMMEND_PLAN, Map.class)));
            body.put("function_call", Map.of("name", "recommend_plan"));

            return webClient.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(response -> {
                        JsonNode root;
                        try {
                            root = objectMapper.readTree(response);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }

                        JsonNode functionCall = root.path("choices").get(0).path("message").path("function_call");
                        if (!functionCall.isMissingNode()) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("recommended_plan", planList.get(0));
                            try {
                                return Mono.just(objectMapper.writeValueAsString(result));
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return Mono.just(root.path("choices").get(0).path("message").path("content").asText());
                    });

        } catch (Exception e) {
            return Mono.just("추천 로직 실행 오류: " + e.getMessage());
        }
    }

    /**
     * 스트리밍 응답에서 content 추출
     */
    private String extractStreamContent(String json) {
        try {
            if (json == null || json.isEmpty() || json.contains("[DONE]")) return "";
            JsonNode root = objectMapper.readTree(json);
            JsonNode contentNode = root.path("choices").get(0).path("delta").path("content");
            if (contentNode.isMissingNode()) return "";
            return contentNode.asText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 일반 응답에서 content 추출
     */
    private String extractContent(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            if (contentNode.isMissingNode()) return "AI 응답 파싱 오류";
            return contentNode.asText();
        } catch (Exception e) {
            return "AI 응답 파싱 오류: " + e.getMessage();
        }
    }

    /**
     * Function Calling JSON 스키마
     */
    private static final String FUNCTION_RECOMMEND_PLAN = """
    {
      "name": "recommend_plan",
      "description": "사용자 정보와 요금제 리스트를 바탕으로 최적의 요금제를 추천합니다.",
      "parameters": {
        "type": "object",
        "properties": {
          "user_info": {
            "type": "object",
            "properties": {
              "voice": {"type": "string"},
              "data": {"type": "string"},
              "sms": {"type": "string"},
              "age": {"type": "string"},
              "type": {"type": "string"},
              "dis": {"type": "string"}
            },
            "required": ["voice", "data", "sms", "age", "type"]
          },
          "plan_list": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "v_tel": {"type": "string"},
                "v_plan_price": {"type": "string"},
                "v_dis_price": {"type": "string"},
                "v_plan_over": {"type": "string"},
                "v_add_name": {"type": "string"},
                "v_plan_name": {"type": "string"},
                "v_plan_display_voice": {"type": "string"},
                "v_display_data": {"type": "string"},
                "v_plan_sms": {"type": "string"},
                "rn": {"type": "string"}
              }
            }
          }
        },
        "required": ["user_info", "plan_list"]
      }
    }
    """;
}
