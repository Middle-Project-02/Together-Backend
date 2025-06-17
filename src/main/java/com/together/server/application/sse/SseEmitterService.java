package com.together.server.application.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
// * SSE 연결을 관리하는 서비스 클래스입니다.
// * <p>
// * 클라이언트별로 SseEmitter를 생성, 저장, 이벤트 전송, 제거 기능을 제공합니다.
// * 연결 유지 시간은 1일로 설정되어 있습니다.
// */
//@Component
//public class SseEmitterService {
//
//    private static final long EMITTER_TIMEOUT = TimeUnit.DAYS.toMillis(1);
//
//    private final Map<String, SseEmitterData> emitters = new ConcurrentHashMap<>();
//
//    /**
//     * 클라이언트 ID를 기반으로 SseEmitter를 생성합니다.
//     * 기존에 해당 클라이언트 ID로 등록된 emitter가 있다면 제거 후 새로 생성합니다.
//     * <p>
//     * 생성된 emitter에 연결 완료 이벤트("connected")를 즉시 전송합니다.
//     *
//     * @param clientId 클라이언트 식별자
//     * @return 새로 생성된 SseEmitter 인스턴스
//     */
//    public SseEmitter createEmitter(String clientId) {
//        if (emitters.containsKey(clientId)) {
//            removeEmitter(clientId);
//        }
//
//        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
//
//        SseEmitterData emitterData = SseEmitterData.of(emitter);
//
//        emitter.onCompletion(() -> removeEmitter(clientId));
//        emitter.onTimeout(() -> removeEmitter(clientId));
//        emitter.onError(throwable -> removeEmitter(clientId));
//
//        emitters.put(clientId, emitterData);
//
//        try {
//            emitter.send(SseEmitter.event().name("connected").data("connected"));
//        } catch (Exception e) {
//            // 로그 처리 필요: 연결 이벤트 전송 실패
//        }
//
//        return emitter;
//    }
//
//    /**
//     * 특정 클라이언트 ID에 이벤트를 전송합니다.
//     * 해당 클라이언트의 emitter가 존재하지 않으면 아무 동작도 하지 않습니다.
//     * 전송 실패 시 emitter를 제거합니다.
//     *
//     * @param clientId  클라이언트 식별자
//     * @param eventType 전송할 이벤트 이름
//     * @param data      전송할 데이터 객체
//     */
//    public void sendEvent(
//            String clientId,
//            String eventType,
//            Object data
//    ) {
//        if (!emitters.containsKey(clientId)) {
//            return;
//        }
//
//        SseEmitterData emitterData = emitters.get(clientId);
//
//        try {
//            emitterData.getEmitter()
//                    .send(
//                            SseEmitter.event()
//                                    .name(eventType)
//                                    .data(data)
//                    );
//
//            emitterData.updateLastEventTime();
//
//        } catch (Exception e) {
//            // 로그 처리 필요: 이벤트 전송 실패
//            removeEmitter(clientId);
//        }
//    git
//
//    /**
//     * 클라이언트 ID에 해당하는 SseEmitter를 종료하고, 내부 저장소에서 제거합니다.
//     * emitter 종료 과정에서 예외 발생해도 무시하고 제거를 진행합니다.
//     *
//     * @param clientId 클라이언트 식별자
//     */
//    public void removeEmitter(String clientId) {
//        try {
//            if (!emitters.containsKey(clientId)) {
//                return;
//            }
//
//            SseEmitterData emitterData = emitters.get(clientId);
//            emitterData.getEmitter().complete();
//
//        } catch (Exception e) {
//            // 로그 처리 필요: emitter 종료 실패
//        } finally {
//            emitters.remove(clientId);
//        }
//    }
//}

//package com.together.server.application.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.Instant;
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
