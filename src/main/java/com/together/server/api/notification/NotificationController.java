package com.together.server.api.notification;

import com.together.server.application.notification.NotificationService;
import com.together.server.application.notification.request.NotificationCreateRequest;
import com.together.server.application.notification.response.NotificationDetailResponse;
import com.together.server.application.notification.response.NotificationSimpleResponse;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "알림장 작성", description = "알림장 작성(어드민 기능 구현 전까지 임시로 사용)")
    public ResponseEntity<ApiResponse<Void>> createNotification(@RequestBody NotificationCreateRequest request) {
        notificationService.createNotification(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    @Operation(summary = "알림장 목록 조회", description = "모든 알림장 목록을 조회")
    public ResponseEntity<ApiResponse<List<NotificationSimpleResponse>>> getAllNotifications(){
        return ResponseEntity.ok(ApiResponse.success(notificationService.getAllNotifications()));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "알림장 상세 조회", description = "알림장 ID를 통해 상세 내용 조회")
    public ResponseEntity<ApiResponse<NotificationDetailResponse>> getNotificationDetail(@PathVariable Long notificationId){
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotificationDetail(notificationId)));
    }


}
