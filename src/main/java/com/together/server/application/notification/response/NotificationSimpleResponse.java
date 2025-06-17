package com.together.server.application.notification.response;

import com.together.server.domain.notification.Notification;

import java.time.Instant;
import java.util.List;

public record NotificationSimpleResponse(String notificationId, String title, String issue, List<String> tags, Instant createdAt) {
    public static NotificationSimpleResponse from(Notification n) {
        return new NotificationSimpleResponse(
                String.valueOf(n.getId()),
                        n.getTitle(),
                        n.getIssue(),
                        n.getTags(),
                        n.getCreatedAt()
        );
    }
}
