package com.together.server.application.notification.request;

import java.util.List;

public record NotificationCreateRequest(String title, String issue, String solution, List<String> tags) {
}
