package com.together.server.api.chat;

import com.together.server.application.chat.SmishingChatService;
import com.together.server.application.chat.request.SmishingChatRequest;
import com.together.server.infra.security.Accessor;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 채팅 관련 API를 제공하는 컨트롤러 클래스입니다.
 *
 * 채팅방 연결, 메시지 전송, 요약 요청 기능을 포함합니다.
 */
@RestController
@RequestMapping("/api/smishing")
public class SmishingChatController {

    private final SmishingChatService SmishingChatService;


    public SmishingChatController(SmishingChatService SmishingChatService) {
        this.SmishingChatService = SmishingChatService;
    }

    @GetMapping("/connect")
    public SseEmitter connect(@AuthenticationPrincipal Accessor accessor) {
        System.out.println("접속한 유저: " + accessor);
        return SmishingChatService.connect(accessor.id());
    }

    @PostMapping("/message")
    public void sendMessage(@AuthenticationPrincipal Accessor accessor, @RequestBody @Valid SmishingChatRequest req) {
        SmishingChatService.sendMessage(accessor.id(), "user", req.content());
    }
}
