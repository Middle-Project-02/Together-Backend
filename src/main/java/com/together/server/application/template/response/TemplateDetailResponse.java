package com.together.server.application.template.response;

public record TemplateDetailResponse(Long id, String title, String content, Integer chatId, Integer planId) {
}
