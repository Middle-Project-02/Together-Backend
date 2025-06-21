package com.together.server.application.notification.response;

import com.together.server.domain.notification.Notification;

import java.time.Instant;
import java.util.List;

public record NotificationDetailResponse(Long notificationId, String title, String summary, String issue, String impact, String solution, List<String> tags, Instant createdAt) {
    public static NotificationDetailResponse from(Notification n) {
        return new NotificationDetailResponse(
                n.getId(),
                n.getTitle(),
                n.getSummary(),
                n.getIssue(),
                n.getImpact(),
                n.getSolution(),
                n.getTags(),
                n.getCreatedAt()
        );
    }
}
