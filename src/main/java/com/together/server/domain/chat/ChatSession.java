package com.together.server.domain.chat;

import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ChatSession {
    private final String userId;
    private final SseEmitter emitter;
    private final Queue<ChatMessage> messages;
    private final Map<String, String> userCondition;

    public ChatSession(String userId, SseEmitter emitter) {
        this.userId = userId;
        this.emitter = emitter;
        this.messages = new LinkedList<>();
        this.userCondition = new ConcurrentHashMap<>();
    }

    public void addMessage(ChatMessage message) {
        System.out.println("[채팅 메시지 저장] " + message.getSender() + ": " + message.getContent());
        messages.add(message);
    }


    public void setCondition(String key, String value) {
        userCondition.put(key, value);
    }
}
