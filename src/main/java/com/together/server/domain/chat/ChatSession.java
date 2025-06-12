package com.together.server.domain.chat;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Queue;
import java.util.LinkedList;
import lombok.Getter;

@Getter
public class ChatSession {
    private final String userId;
    private final String roomId;
    private final SseEmitter emitter;
    private final Queue<ChatMessage> messages;

    public ChatSession(String userId, String roomId, SseEmitter emitter) {
        this.userId = userId;
        this.roomId = roomId;
        this.emitter = emitter;
        this.messages = new LinkedList<>();
    }


    public void addMessage(ChatMessage message) {
        messages.add(message);
    }
}
