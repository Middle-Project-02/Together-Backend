package com.together.server.application.notification.response;

import java.time.Instant;
import java.util.List;

public record NotificationSimpleResponse(Long notificationId, String title, String issue, List<String> tags, Instant createdAt) {
}
