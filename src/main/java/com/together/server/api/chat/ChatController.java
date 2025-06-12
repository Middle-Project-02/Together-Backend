package com.together.server.api.chat;

import com.together.server.application.chat.SseChatService;
import com.together.server.application.chat.request.ChatRequest;
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
@RequestMapping("/api/chat")
public class ChatController {

    private final SseChatService sseChatService;

    /**
     * 생성자 - SseChatService 주입
     *
     * @param sseChatService SSE 기반 채팅 서비스
     */
    public ChatController(SseChatService sseChatService) {
        this.sseChatService = sseChatService;
    }

    /**
     * 클라이언트와 SSE 연결을 생성하여 채팅방에 연결합니다.
     *
     * @param accessor 인증된 사용자 정보
     * @param roomId 채팅방 식별자
     * @return SseEmitter 객체 (서버-센트 이벤트 연결)
     */
    @GetMapping("/connect/{roomId}")
    public SseEmitter connect(
            @AuthenticationPrincipal Accessor accessor,
            @PathVariable String roomId
    ) {
        return sseChatService.connect(accessor.id(), roomId);
    }

    /**
     * 채팅방에 메시지를 전송합니다.
     *
     * @param accessor 인증된 사용자 정보
     * @param roomId 채팅방 식별자
     * @param req 메시지 요청 데이터 (content 포함)
     */
    @PostMapping("/message/{roomId}")
    public void sendMessage(
            @AuthenticationPrincipal Accessor accessor,
            @PathVariable String roomId,
            @RequestBody @Valid ChatRequest req
    ) {
        sseChatService.sendMessage(accessor.id(), roomId, "user", req.content());
    }

    /**
     * 채팅방 메시지 요약을 요청합니다.
     *
     * @param accessor 인증된 사용자 정보
     * @param roomId 채팅방 식별자
     */
    @PostMapping("/summary/{roomId}")
    public void requestSummary(
            @AuthenticationPrincipal Accessor accessor,
            @PathVariable String roomId
    ) {
        sseChatService.requestSummary(accessor.id(), roomId);
    }
}
