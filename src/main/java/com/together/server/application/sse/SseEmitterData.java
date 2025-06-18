package com.together.server.application.sse;

import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.Instant;

@Getter
public class SseEmitterData {
    private final SseEmitter emitter;
    private final Instant createdAt;
    private Instant lastEventTime;

    public SseEmitterData(SseEmitter emitter, Instant createdAt, Instant lastEventTime) {
        this.emitter = emitter;
        this.createdAt = createdAt;
        this.lastEventTime = lastEventTime;
    }

    public static SseEmitterData of(SseEmitter emitter) {
        Instant now = Instant.now();
        return new SseEmitterData(emitter, now, now);
    }

    public void updateLastEventTime() {
        this.lastEventTime = Instant.now();
    }
}
