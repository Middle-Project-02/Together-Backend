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
public class SseChatService {

    /** 사용자별로 방(room) 별 채팅 세션을 저장하는 맵 */
    private final Map<String, Map<String, ChatSession>> chatSessions = new ConcurrentHashMap<>();

    /** SSE 이벤트 전송을 처리하는 서비스 */
    private final SseEmitterService sseEmitterService;

    /** OpenAI GPT API 클라이언트 */
    private final OpenAiChatClient openAiChatClient;

    /**
     * 생성자
     *
     * @param sseEmitterService SSE 전송을 위한 서비스
     * @param openAiChatClient OpenAI GPT 호출을 위한 클라이언트
     */
    public SseChatService(
            SseEmitterService sseEmitterService,
            OpenAiChatClient openAiChatClient
    ) {
        this.sseEmitterService = sseEmitterService;
        this.openAiChatClient = openAiChatClient;
    }

    /**
     * 사용자가 채팅방에 연결할 때 SSE 연결을 생성하고 ChatSession을 등록합니다.
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     * @return 생성된 SseEmitter
     */
    public SseEmitter connect(String userId, String roomId) {
        chatSessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        SseEmitter emitter = sseEmitterService.createEmitter(userId + "_" + roomId);
        ChatSession session = new ChatSession(userId, roomId, emitter);
        chatSessions.get(userId).put(roomId, session);
        return emitter;
    }

    /**
     * 사용자의 메시지를 채팅 세션에 저장하고, OpenAI GPT를 통해 스미싱 분석 결과를 SSE로 스트리밍 전송합니다.
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     * @param sender 메시지 발신자 (예: 사용자 또는 시스템)
     * @param content 메시지 내용
     */
    public void sendMessage(String userId, String roomId, String sender, String content) {
        ChatSession session = getSession(userId, roomId);
        if (session == null) return;

        session.addMessage(new ChatMessage(sender, content));
        sendEvent(session, "question", content);

        String promptForMessage =
                "당신은 스미싱 탐지 전문가 AI입니다. 사용자가 보낸 문자메시지가 스미싱인지 아닌지를 분석하여 아래 형식으로 답변해주세요.\n"
                        + "\n"
                        + "- 스미싱 가능성: 높음 / 보통 / 낮음 (세 단계 중 하나만 선택)\n"
                        + "- 판단 근거: 왜 그렇게 판단했는지 명확히 설명 (URL 포함 여부, 개인정보 요구 여부, 긴급성 유도 등)\n"
                        + "- 사용자 주의사항: 사용자가 어떻게 대응해야 하는지 간단히 안내 (예: 링크 클릭 금지, 삭제 권장 등)\n"
                        + "\n"
                        + "아래는 사용자가 보낸 문자메시지입니다:\n"
                        + "\n"
                        + "\"\"\"\n"
                        + content
                        + "\n\"\"\"\n"
                        + "\n"
                        + "이제 분석 결과를 위 형식에 맞추어 출력하세요.";

        Disposable subscription = openAiChatClient.streamChatCompletion(promptForMessage)
                .subscribe(
                        chunk -> sendEvent(session, "stream_chat", chunk),
                        error -> sseEmitterService.removeEmitter(userId + "_" + roomId),
                        () -> sendEvent(session, "done", "done")
                );

        // 추후 취소 기능을 구현하려면 subscription을 저장해두고 관리 필요
    }

    /**
     * 현재 채팅 세션의 대화 내용을 요약 요청하여 응답을 SSE로 전송합니다.
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     */
    public void requestSummary(String userId, String roomId) {
        ChatSession session = getSession(userId, roomId);
        if (session == null) return;

        String chatHistory = session.getMessages().stream()
                .map(msg -> msg.getSender() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));

        String promptForSummary = "아래 대화 기록을 한 단어로 요약해주세요:\n" + chatHistory;

        String summary = openAiChatClient.getChatCompletion(promptForSummary);
        sendEvent(session, "summary", summary);
    }

    /**
     * 특정 사용자-방의 채팅 세션을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     * @return ChatSession 객체 (없으면 null)
     */
    private ChatSession getSession(String userId, String roomId) {
        Map<String, ChatSession> userSessions = chatSessions.get(userId);
        if (userSessions == null) return null;
        return userSessions.get(roomId);
    }

    /**
     * SSE 이벤트를 전송합니다. 전송 실패 시 emitter를 제거합니다.
     *
     * @param session 대상 채팅 세션
     * @param eventType 이벤트 타입
     * @param data 전송할 데이터
     */
    private void sendEvent(ChatSession session, String eventType, String data) {
        try {
            sseEmitterService.sendEvent(session.getUserId() + "_" + session.getRoomId(), eventType, data);
        } catch (Exception e) {
            sseEmitterService.removeEmitter(session.getUserId() + "_" + session.getRoomId());
        }
    }
}
