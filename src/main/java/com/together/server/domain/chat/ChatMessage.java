package com.together.server.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessage {
    private final String sender;
    private final String content;
}
