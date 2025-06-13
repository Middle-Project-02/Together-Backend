package com.together.server.application.notification.response;

import java.time.Instant;

public record NotificationDetailResponse(Long notificationId, String title, String content, Instant createdAt) {
}
