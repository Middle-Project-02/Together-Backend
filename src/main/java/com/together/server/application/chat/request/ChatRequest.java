package com.together.server.application.chat.request;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "content는 빈 값일 수 없습니다.")
        String content
) {
}
