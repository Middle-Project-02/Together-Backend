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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        return emitter;
    }

    public void sendMessage(String userId, String sender, String content) {
        ChatSession session = chatSessions.get(userId);
        if (session == null) return;

        session.addMessage(new ChatMessage(sender, content));
        sendEvent(userId, "question", content);

        parseAndSetCondition(content, session);

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
                            "- data (string): 월 평균 데이터 사용량 (단위: GB, 무제한은 999999)",
                            "- sms (string): 월 평균 문자 발송량 (단위: 건, 무제한은 999999)",
                            "- age (string): 연령대 (성인:20, 청소년:15, 실버:65)",
                            "- type (string): 통신 서비스 타입 (3G:2, LTE:4, 5G:5)",
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
                StringBuilder chunkBuffer = new StringBuilder();  // 버퍼 선언
                Disposable subscription = openAiChatClient.streamMultiturnChatCompletion(messages)
                        .subscribe(
                                chunk -> {
                                    chunkBuffer.append(chunk);
                                    if (chunk.endsWith(" ") || chunk.endsWith("\n")) {
                                        sendEvent(userId, "stream_chat", chunkBuffer.toString());
                                        chunkBuffer.setLength(0); // 초기화
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
            sendEvent(userId, "stream_chat", "답변 감사합니다! \ud83d\ude0a\n말씀해주신 정보를 바탕으로 요금제를 추천해드릴게요. 잠시만 기다려주세요.");
//            sendEvent(userId, "stream_chat", formatJson(session.getUserCondition()));
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
                sseEmitterService.sendEvent(userId, "recommend_result", List.of(plan));
                sendEvent(userId, "done", "done");

            } catch (Exception e) {
                log.error("요금제 추천 중 오류", e);
                sendEvent(userId, "stream_chat", "요금제 추천 중 문제가 발생했습니다.");
                sendEvent(userId, "done", "done");
            }
        }
    }

    private void parseAndSetCondition(String content, ChatSession session) {
        String lowerContent = content.toLowerCase();

        String[] parts = content.split(":");
        if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim();
            if (REQUIRED_KEYS.contains(key)) {
                session.setCondition(key, value);
                log.info("[조건 저장됨] {} = {}", key, value);
                return;
            }
        }

        if (lowerContent.contains("통화")) {
            int voiceValue = extractMinutes(content);
            if (voiceValue > 0) {
                session.setCondition("voice", String.valueOf(voiceValue));
                log.info("[자연어 통화량 저장] voice = {}", voiceValue);
            }
        }

        if (lowerContent.contains("데이터") || lowerContent.contains("인터넷")) {
            int dataValue = extractData(content);
            if (dataValue > 0) {
                session.setCondition("data", String.valueOf(dataValue));
                log.info("[자연어 데이터 저장] data = {}", dataValue);
            }
        }

        if (lowerContent.contains("문자")) {
            int smsValue = extractCount(content);
            if (smsValue > 0) {
                session.setCondition("sms", String.valueOf(smsValue));
                log.info("[자연어 문자 저장] sms = {}", smsValue);
            }
        }

        if (lowerContent.contains("살") || lowerContent.contains("나이")) {
            int ageValue = extractAge(content);
            if (ageValue > 0) {
                session.setCondition("age", String.valueOf(ageValue));
                log.info("[자연어 나이 저장] age = {}", ageValue);
            }
        }

        // 통신타입
        if (lowerContent.contains("5g")) {
            session.setCondition("type", "5");
            log.info("[자연어 타입 저장] type = 5");
        } else if (lowerContent.contains("lte")) {
            session.setCondition("type", "4");
            log.info("[자연어 타입 저장] type = 4");
        } else if (lowerContent.contains("3g")) {
            session.setCondition("type", "2");
            log.info("[자연어 타입 저장] type = 2");
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

    private String formatJson(Map<String, String> data) {
        return data.entrySet().stream()
                .map(e -> "\"%s\": \"%s\"".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining(",\n", "{\n", "\n}"));
    }

    private int extractMinutes(String content) {
        Matcher matcher = Pattern.compile("(\\d+)\\s*(분|시간)").matcher(content);
        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            if (unit.equals("시간")) {
                return value * 60;
            } else {
                return value;
            }
        }
        if (content.contains("무제한")) {
            return 999999;
        }
        return 0;
    }

    private int extractData(String content) {
        Matcher matcher = Pattern.compile("(\\d+)\\s*(gb|기가)").matcher(content.toLowerCase());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        if (content.contains("무제한")) {
            return 999999;
        }
        return 0;
    }

    private int extractCount(String content) {
        Matcher matcher = Pattern.compile("(\\d+)\\s*(건)").matcher(content);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        if (content.contains("무제한")) {
            return 999999;
        }
        return 0;
    }

    private int extractAge(String content) {
        Matcher matcher = Pattern.compile("(\\d+)\\s*살").matcher(content);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        if (content.contains("성인")) {
            return 20;
        }
        if (content.contains("청소년")) {
            return 15;
        }
        if (content.contains("실버") || content.contains("노인")) {
            return 65;
        }
        return 0;
    }


    public void requestSummary(String userId) {
        ChatSession session = chatSessions.get(userId);
        if (session == null) return;
        String history = session.getMessages().stream()
                .map(msg -> msg.getSender() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));
        String prompt = "아래 대화 기록을 한 문장으로 요약해주세요:\n" + history;
        String summary = openAiChatClient.generateSummaryResponse(prompt);
        sendEvent(userId, "summary", summary);
    }
}