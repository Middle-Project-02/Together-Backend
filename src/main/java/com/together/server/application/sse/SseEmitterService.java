package com.together.server.application.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class SseEmitterService {
    private static final long EMITTER_TIMEOUT = TimeUnit.DAYS.toMillis(1);
    private final Map<String, SseEmitterData> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String clientId) {
        if (emitters.containsKey(clientId)) removeEmitter(clientId);
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        SseEmitterData emitterData = SseEmitterData.of(emitter);
        emitters.put(clientId, emitterData);
        emitter.onCompletion(() -> removeEmitter(clientId));
        emitter.onTimeout(() -> removeEmitter(clientId));
        emitter.onError(throwable -> removeEmitter(clientId));
        try { emitter.send(SseEmitter.event().name("connected").data("connected")); }
        catch (Exception e) { /* 로그 처리 필요 */ }
        return emitter;
    }

    public void sendEvent(String clientId, String eventType, Object data) {
        if (!emitters.containsKey(clientId)) return;
        SseEmitterData emitterData = emitters.get(clientId);
        try {
            emitterData.getEmitter().send(SseEmitter.event().name(eventType).data(data));
            emitterData.updateLastEventTime();
        } catch (Exception e) {
            removeEmitter(clientId);
        }
    }

    public void removeEmitter(String clientId) {
        try {
            if (!emitters.containsKey(clientId)) return;
            SseEmitterData emitterData = emitters.get(clientId);
            emitterData.getEmitter().complete();
        } catch (Exception e) { /* 로그 처리 필요 */ }
        finally { emitters.remove(clientId); }
    }
}
