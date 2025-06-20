package com.together.server.application.notification.response;

import com.together.server.domain.notification.Notification;

import java.time.Instant;
import java.util.List;

public record NotificationSimpleResponse(Long notificationId, String title, String summary, List<String> tags, Instant createdAt) {
    public static NotificationSimpleResponse from(Notification n) {
        return new NotificationSimpleResponse(
                n.getId(),
                n.getTitle(),
                n.getSummary(),
                n.getTags(),
                n.getCreatedAt()
        );
    }
}
