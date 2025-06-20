package com.together.server.application.notification.request;

import java.util.List;

public record NotificationCreateRequest(String title, String summary, String issue, String impact, String solution, List<String> tags) {
}
