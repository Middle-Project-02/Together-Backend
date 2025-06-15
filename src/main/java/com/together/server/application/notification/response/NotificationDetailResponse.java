package com.together.server.application.notification.response;

import java.time.Instant;
import java.util.List;

public record NotificationDetailResponse(Long notificationId, String title, String issue, String solution, List<String> tags, Instant createdAt) {
}
