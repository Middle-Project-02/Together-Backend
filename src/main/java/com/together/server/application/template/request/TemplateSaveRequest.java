package com.together.server.application.template.request;

public record TemplateSaveRequest(Long memberId, Integer chatId, String title, String content, Integer planId) {
}
