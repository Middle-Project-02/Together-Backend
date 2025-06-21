package com.together.server.application.chat;

import com.together.server.application.sse.SseEmitterService;
import com.together.server.domain.chat.ChatMessage;
import com.together.server.domain.chat.ChatSession;
import com.together.server.domain.plan.SmartChoicePlan;
import com.together.server.infra.openai.OpenAiChatClient;
import com.together.server.infra.smartchoice.SmartChoiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SseChatService {

    private final Map<String, ChatSession> chatSessions = new ConcurrentHashMap<>();
    private final SseEmitterService sseEmitterService;
    private final OpenAiChatClient openAiChatClient;
    private final SmartChoiceClient smartChoiceClient;

    private static final Set<String> REQUIRED_KEYS = Set.of("voice", "data", "sms", "age", "type");

    public SseChatService(
            SseEmitterService sseEmitterService,
            OpenAiChatClient openAiChatClient,
            SmartChoiceClient smartChoiceClient
    ) {
        this.sseEmitterService = sseEmitterService;
        this.openAiChatClient = openAiChatClient;
        this.smartChoiceClient = smartChoiceClient;
    }

    public SseEmitter connect(String userId) {
        SseEmitter emitter = sseEmitterService.createEmitter(userId);
        ChatSession session = new ChatSession(userId, emitter);
        chatSessions.put(userId, session);
        sendEvent(userId, "answer", "안녕하세요! 요금제 추천 도우미예요 😊 궁금한 점을 물어봐 주세요!");
        return emitter;
    }

    public void sendMessage(String userId, String sender, String content) {
        ChatSession session = chatSessions.get(userId);
        if (session == null) return;

        session.addMessage(new ChatMessage(sender, content));
        sendEvent(userId, "question", content);

        String conditionJson = openAiChatClient.extractUserConditions(content);
        Map<String, String> extracted = openAiChatClient.parseConditionJson(conditionJson);
        for (String key : REQUIRED_KEYS) {
            if (extracted.containsKey(key)) {
                session.setCondition(key, extracted.get(key));
            }
        }
        log.info("[사용자 조건 누적 상태] userId={} → {}", userId, session.getUserCondition());

        Set<String> missing = REQUIRED_KEYS.stream()
                .filter(key -> !session.getUserCondition().containsKey(key))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of(
                    "role", "system",
                    "content", String.join("\n",
                            "너는 '세계 최고의 요금제 추천 챗봇'이다.",
                            "너의 임무는 사용자와 자연스럽고 유쾌한 대화를 나누며, 요금제를 추천하기 위한 아래 5가지 정보를 수집하는 것이다.",
                            "\u2728 필수 정보:",
                            "- voice (string): 월 평균 통화량 (단위: 분, 무제한은 999999)",
                            "- data (string): 월 평균 데이터 사용량 (단위: MB, 무제한은 999999)",
                            "- sms (string): 월 평균 문자 발송량 (단위: 건, 무제한은 999999)",
                            "- age (string): 연령대 (성인:20, 청소년:18, 실버:65)",
                            "- type (string): 통신 서비스 타입 (3G:2, LTE:3, 5G:6)",
                            "사용자의 응답이 모호할 경우 예시를 통해 근사값을 유도하고, 대화 흐름을 부드럽게 유지해야 한다.",
                            "한 번에 하나의 질문만 하며, 무례하거나 기계적으로 느껴지지 않도록 한다."
                    )
            ));

            if (!session.getUserCondition().isEmpty()) {
                StringBuilder summary = new StringBuilder("지금까지 사용자가 알려준 정보:\n");
                session.getUserCondition().forEach((k, v) -> summary.append("- ").append(k).append(": ").append(v).append("\n"));
                messages.add(Map.of("role", "system", "content", summary.toString()));
            }

            messages.addAll(
                    session.getMessages().stream()
                            .map(msg -> Map.of("role", msg.getSender(), "content", msg.getContent()))
                            .toList()
            );

            try {
                StringBuilder chunkBuffer = new StringBuilder();
                Disposable subscription = openAiChatClient.streamMultiturnChatCompletion(messages)
                        .subscribe(
                                chunk -> {
                                    chunkBuffer.append(chunk);
                                    if (chunk.endsWith(" ") || chunk.endsWith("\n")) {
                                        sendEvent(userId, "stream_chat", chunkBuffer.toString());
                                        chunkBuffer.setLength(0);
                                    }
                                },
                                error -> {
                                    log.error("OpenAI 스트림 응답 오류", error);
                                    sseEmitterService.removeEmitter(userId);
                                },
                                () -> {
                                    if (chunkBuffer.length() > 0) {
                                        sendEvent(userId, "stream_chat", chunkBuffer.toString());
                                    }
                                    sendEvent(userId, "done", "done");
                                }
                        );
            } catch (Exception e) {
                log.error("OpenAI 호출 실패", e);
                sendEvent(userId, "stream_chat", "AI 응답에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
                sendEvent(userId, "done", "done");
            }
        } else {
            sendEvent(userId, "stream_chat", "아래는 가장 적합한 요금제입니다!" +
                    "");
            try {
                List<Map<String, String>> planList = smartChoiceClient.getPlans(
                        session.getUserCondition().get("voice"),
                        session.getUserCondition().get("data"),
                        session.getUserCondition().get("sms"),
                        session.getUserCondition().get("age"),
                        session.getUserCondition().get("type")
                );

                Optional<Map<String, String>> lowestLguPlan = planList.stream()
                        .filter(plan -> "LGU+".equalsIgnoreCase(plan.get("telecom")))
                        .min(Comparator.comparingInt(p -> Integer.parseInt(p.get("price"))));

                if (lowestLguPlan.isEmpty()) {
                    sendEvent(userId, "stream_chat", "LGU+ 통신사 요금제를 찾을 수 없습니다.");
                    sendEvent(userId, "done", "done");
                    return;
                }

                SmartChoicePlan plan = SmartChoicePlan.from(lowestLguPlan.get());
                session.setRecommendedPlan(plan);
                sseEmitterService.sendEvent(userId, "recommend_result", List.of(plan));
                sendEvent(userId, "done", "done");

            } catch (Exception e) {
                log.error("요금제 추천 중 오류", e);
                sendEvent(userId, "stream_chat", "요금제 추천 중 문제가 발생했습니다.");
                sendEvent(userId, "done", "done");
            }
        }
    }

    private void sendEvent(String userId, String eventType, String data) {
        try {
            sseEmitterService.sendEvent(userId, eventType, data);
        } catch (Exception e) {
            log.error("SSE 이벤트 전송 실패", e);
            sseEmitterService.removeEmitter(userId);
        }
    }

    public void requestSummary(String userId) {
        ChatSession session = chatSessions.get(userId);
        if (session == null) return;

        SmartChoicePlan plan = session.getRecommendedPlan();
        if (plan == null) {
            sendEvent(userId, "summary", "추천된 요금제가 없습니다. 먼저 조건을 입력해 추천을 받아주세요.");
            sendEvent(userId, "done", "done");
            return;
        }

        String prompt = String.format("""
        다음은 고객에게 추천된 요금제입니다.
        아래 정보를 바탕으로 시니어 고객이 대리점에 방문했을 때 직원에게 보여줄 수 있는 설명을 만들어주세요.

        - 요금제명: %s
        - 통신사: %s
        - 음성: %s분
        - 문자: %s건
        - 데이터: %sGB
        - 월요금: %s원

        [요구사항]
        1. 첫 번째 줄에는 아래 형식으로 작성해주세요:
           제목: %s
        2. 두 번째 줄부터는 다음 형식으로 작성해주세요:
           내용: (해당 요금제의 요약 설명과 함께 대리점에서 직원에게 어떻게 말하면 되는지 안내 문구를 포함해주세요)
        3. 시니어분들이 이해하기 쉽고 자연스럽게 표현해주세요.
        """,
                plan.getPlanName(),
                plan.getTelecom(),
                plan.getVoice(),
                plan.getSms(),
                plan.getData(),
                plan.getPrice(),
                plan.getPlanName()
        );

        String gptResponse = openAiChatClient.generateSummaryResponse(prompt);

        String title = plan.getPlanName();
        String content = "";

        boolean foundContent = false;
        StringBuilder bodyBuilder = new StringBuilder();

        for (String line : gptResponse.split("\n")) {
            if (line.startsWith("내용:")) {
                foundContent = true;
                bodyBuilder.append(line.substring("내용:".length()).trim()).append("\n");
            } else if (foundContent) {
                bodyBuilder.append(line).append("\n");
            }
        }

        content = bodyBuilder.toString().trim();

        Map<String, String> result = Map.of(
                "title", title,
                "content", content
        );

        sseEmitterService.sendEvent(userId, "summary", result);
        sendEvent(userId, "done", "done");
    }
}
