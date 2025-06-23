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
                            "사용자에게 **하루 기준으로** 질문하고, 대답을 바탕으로 한 달 예상 사용량을 계산해.",
                            "",
                            "🎯 수집해야 할 정보:",
                            "- voice (통화): 하루 통화 시간 (예: 10분)",
                            "- data (데이터): 유튜브/인터넷 이용 시간 (예: 하루 1시간)",
                            "- sms (문자): 하루 문자 건수 (예: 5건)",
                            "- age (연령): 나이 또는 연령대 (예: 75세)",
                            "- type (통신망): 사용 중인 휴대폰 유형 (예: LTE, 5G)",
                            "",
                            "⛔ 어려운 용어나 숫자 대신, 일상적인 활동 기준으로 예시를 들어 설명해줘.",
                            "❗ 반드시 한 번에 하나의 항목만 질문해야 해. 예를 들어 통화 시간과 문자 건수를 한 문장에서 묻지 마.",
                            "❌ 잘못된 예시: 하루에 통화랑 문자 얼마나 하세요?",
                            "✅ 올바른 예시: 하루에 통화를 얼마나 하세요?"
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
            sendEvent(userId, "stream_chat", "답변 감사합니다! 😊\n말씀해주신 정보를 바탕으로 아래 가장 적합한 요금제를 추천드립니다!");
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
이 정보를 바탕으로 **시니어 고객이 대리점에 방문해서 직원에게 보여줄 수 있는 안내 문구**를 만들어주세요.

- 요금제명: %s
- 통신사: %s
- 음성: %s분
- 문자: %s건
- 데이터: %sGB
- 월요금: %s원

[요구사항]
1. 첫 번째 줄은 아래 형식으로 작성해주세요:
   제목: %s

2. 두 번째 줄부터는 아래 형식으로 작성해주세요:
   내용: (1~2줄로 요금제의 핵심 특징을 요약하고,  
           다음 줄에 시니어 고객이 대리점에서 말할 수 있는 문장을 따옴표 안에 작성해주세요,  
           마지막 줄에는 직원에게 이 문장을 보여주면 된다는 안내 문구도 덧붙여주세요)

3. 예시:
   제목: LTE 시니어 16.5  
   내용: 월 16500원에 70분 통화, 100건 문자, 300MB 데이터를 사용할 수 있어요.  
         "LGU+ LTE 시니어 16.5 요금제로 바꿔주세요."  
         이 문장을 직원분께 보여주시면 바로 도와주실 거예요.

4. 직원이 문장만 읽어도 어떤 요금제인지 바로 이해할 수 있도록 작성해주세요.
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
