package com.together.server.application.template.request;

public record TemplateSaveRequest(Integer chatId, String title, String content, Integer planId) {
}
