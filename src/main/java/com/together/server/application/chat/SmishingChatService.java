package com.together.server.application.chat;

import com.together.server.application.sse.SseEmitterService;
import com.together.server.domain.chat.ChatMessage;
import com.together.server.domain.chat.ChatSession;
import com.together.server.infra.openai.OpenAiChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 서비스 간 서버 전송(EventStream) 기반의 실시간 채팅 기능을 제공하는 서비스입니다.
 * 사용자별 채팅 세션을 관리하며, OpenAI API를 통한 스미싱 분석 및 요약 요청 기능도 포함합니다.
 */
@Service
public class SmishingChatService {

    private final Map<String, ChatSession> smishingChatSessions = new ConcurrentHashMap<>();
    private final SseEmitterService sseEmitterService;
    private final OpenAiChatClient openAiChatClient;

    public SmishingChatService(
            SseEmitterService sseEmitterService,
            OpenAiChatClient openAiChatClient
    ) {
        this.sseEmitterService = sseEmitterService;
        this.openAiChatClient = openAiChatClient;
    }

    public SseEmitter connect(String userId) {
        SseEmitter emitter = sseEmitterService.createEmitter(userId);
        ChatSession session = new ChatSession(userId, null, emitter);
        smishingChatSessions.put(userId, session);
        return emitter;
    }

    public void sendMessage(String userId, String sender, String content) {
        ChatSession session = getSession(userId);
        if (session == null) return;

        session.addMessage(new ChatMessage(sender, content));
        sendEvent(session, "question", content);

        String promptForMessage =

                        "당신은 스미싱 탐지 전문가 AI입니다. 사용자가 보낸 문자 메시지를 정밀하게 분석하여 스미싱(스마트폰 피싱 사기) 여부를 판단하고, 아래 형식에 맞춰 상세하고 전문적으로 설명해주세요.\n" +
                                "\n" +
                                "**매우 중요: 반드시 올바른 띄어쓰기와 자연스럽고 정확한 한국어 문법을 지켜서 작성해주세요. 어색한 표현, 기계적인 문장, 문법 오류는 절대 피해야 합니다.**\n" +
                                "\n" +
                                "아래 형식으로 응답하세요:\n" +
                                "\n" +
                                "- **스미싱 가능성**: 높음 / 보통 / 낮음 (세 단계 중 하나만 선택)\n" +
                                "\n" +
                                "- **스미싱 의심 요소**: (가능성이 높음/보통인 경우만 작성)\n" +
                                "  각 의심 요소를 번호와 함께 **굵은 글씨**로 소제목을 만들고, 그 아래에 구체적인 분석 내용을 상세히 설명하세요.\n" +
                                "  \n" +
                                "  분석할 주요 항목들:\n" +
                                "  • 발신자 정보 (국외발신, 번호 형태, 공식 기관 사칭 등)\n" +
                                "  • URL/링크 (도메인 신뢰성, 단축URL 여부, 의심스러운 확장자)\n" +
                                "  • 메시지 내용 (개인정보 요구, 긴급성 유도, 금전 요구, 협박성 표현)\n" +
                                "  • 문법/맞춤법 (부자연스러운 표현, 오타, 기계번역체)\n" +
                                "  • 구체성 부족 (세부 정보 누락, 일반적이지 않은 표현)\n" +
                                "  • 수신자 특정성 (개인화 정보 부족, 대량 발송 의심)\n" +
                                "\n" +
                                "- **정상 메시지일 가능성**: (가능성이 낮음인 경우만 작성)\n" +
                                "  정상적인 메시지로 판단되는 근거들을 구체적으로 설명하세요.\n" +
                                "\n" +
                                "- **사용자 주의사항**: \n" +
                                "  해당 문자에서 실제로 필요한 조치만 선택하여 간단명료하게 안내하세요.\n" +
                                "  • 의심스러운 **링크 클릭 금지**\n" +
                                "  • 절대 **개인정보 입력하지 말 것**\n" +
                                "  • 출처가 불명확한 경우 **문자 삭제 및 무시**\n" +
                                "  • 필요시 **118 또는 경찰청 사이버수사대(privacy.go.kr) 신고**\n" +
                                "  • 공식 기관 사칭 의심 시 **해당 기관에 직접 문의**\n" +
                                "\n" +
                                "- **최종 안내**: 이 분석 결과는 참고용이며 100% 정확하지 않을 수 있습니다. 의심되는 경우에는 반드시 공식 기관이나 고객센터를 통해 사실 여부를 확인하시기 바랍니다.\n" +
                                "\n" +
                                "사용자가 문자 메시지를 보내면, 위 분석 기준에 따라 각 요소를 꼼꼼히 검토하고 전문적이고 구체적인 분석 결과를 출력하세요."

                        + "\n"
                        + "아래는 사용자가 보낸 문자메시지입니다:\n"
                        + "\n"
                        + "\"\"\"\n"
                        + content
                        + "\n\"\"\"\n"
                        + "\n"
                        + "이제 분석 결과를 위 형식에 맞추어 출력하세요.";


        final StringBuilder chunkBuffer = new StringBuilder();

        Disposable subscription = openAiChatClient.streamChatCompletion(promptForMessage)
                .subscribe(
                        chunk -> {
                            chunkBuffer.append(chunk);

                            if (chunk.endsWith(" ") || chunk.endsWith("\n")) {
                                sendEvent(session, "stream_chat", chunkBuffer.toString());
                                chunkBuffer.setLength(0); // 초기화
                            }
                        },
                        error -> {
                            System.err.println("OpenAI API Error: " + error.getMessage());
                            sendEvent(session, "error", "AI 응답 중 오류가 발생했습니다: " + error.getMessage());
                        },
                        () -> {
                            if (chunkBuffer.length() > 0) {
                                sendEvent(session, "stream_chat", chunkBuffer.toString());
                                chunkBuffer.setLength(0);
                            }
                            sendEvent(session, "done", "done");
                        }
                );


    }


    private ChatSession getSession(String userId) {
        return smishingChatSessions.get(userId);
    }

    private void sendEvent(ChatSession session, String eventType, String data) {
        try {
            sseEmitterService.sendEvent(session.getUserId(), eventType, data);
        } catch (Exception e) {
            System.err.println("SSE send error: " + e.getMessage() + " - Event: " + eventType + ", Data: " + data);
            // 에러 발생해도 바로 제거하지 말고 로그만 남기기
        }
    }
}