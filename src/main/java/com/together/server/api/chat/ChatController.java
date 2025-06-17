package com.together.server.api.chat;

import com.together.server.application.chat.SseChatService;
import com.together.server.application.chat.request.ChatRequest;
import com.together.server.infra.security.Accessor;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final SseChatService sseChatService;

    public ChatController(SseChatService sseChatService) {
        this.sseChatService = sseChatService;
    }

    @GetMapping("/connect")
    public SseEmitter connect(@AuthenticationPrincipal Accessor accessor) {
        return sseChatService.connect(accessor.id());
    }

    @PostMapping("/message")
    public void sendMessage(
            @AuthenticationPrincipal Accessor accessor,
            @RequestBody @Valid ChatRequest req
    ) {
        sseChatService.sendMessage(accessor.id(), "user", req.content());
    }

    @PostMapping("/summary")
    public void requestSummary(@AuthenticationPrincipal Accessor accessor) {
        sseChatService.requestSummary(accessor.id());
    }
}