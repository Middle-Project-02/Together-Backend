package com.together.server.application.sse;

import java.time.Instant;
import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SseEmitterData는 SseEmitter와 관련된 메타데이터를 관리하는 클래스입니다.
 * <p>
 * 이 클래스는 SseEmitter 인스턴스, 생성 시각, 마지막 이벤트 전송 시각을 포함합니다.
 * {@link #updateLastEventTime()} 메서드를 통해 마지막 이벤트 시각을 갱신할 수 있습니다.
 */
@Getter
public class SseEmitterData {

    private final SseEmitter emitter;
    private final Instant createdAt;
    private Instant lastEventTime;

    /**
     * 생성자 - 모든 필드를 초기화합니다.
     *
     * @param emitter       SSE 통신을 위한 SseEmitter 인스턴스
     * @param createdAt     SseEmitter 생성 시각
     * @param lastEventTime 마지막 이벤트 전송 시각
     */
    public SseEmitterData(
            SseEmitter emitter,
            Instant createdAt,
            Instant lastEventTime
    ) {
        this.emitter = emitter;
        this.createdAt = createdAt;
        this.lastEventTime = lastEventTime;
    }

    /**
     * 정적 팩토리 메서드로 현재 시각을 기준으로 SseEmitterData 인스턴스를 생성합니다.
     *
     * @param emitter SSE 통신을 위한 SseEmitter 인스턴스
     * @return 새로운 SseEmitterData 객체
     */
    public static SseEmitterData of(SseEmitter emitter) {
        Instant now = Instant.now();
        return new SseEmitterData(emitter, now, now);
    }

    /**
     * 마지막 이벤트 전송 시각을 현재 시각으로 갱신합니다.
     */
    public void updateLastEventTime() {
        this.lastEventTime = Instant.now();
    }
}
