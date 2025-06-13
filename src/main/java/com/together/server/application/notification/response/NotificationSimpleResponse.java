package com.together.server.application.notification.response;

import java.time.Instant;

public record NotificationSimpleResponse(Long notificationId, String title, Instant createdAt) {
}

