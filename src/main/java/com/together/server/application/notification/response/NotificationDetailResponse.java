package com.together.server.application.notification.response;

import com.together.server.domain.notification.Notification;

import java.time.Instant;
import java.util.List;

public record NotificationDetailResponse(String notificationId, String title, String issue, String solution, List<String> tags, Instant createdAt) {
    public static NotificationDetailResponse from(Notification n) {
        return new NotificationDetailResponse(
                String.valueOf(n.getId()),
                n.getTitle(),
                n.getIssue(),
                n.getSolution(),
                n.getTags(),
                n.getCreatedAt()
        );
    }
}
