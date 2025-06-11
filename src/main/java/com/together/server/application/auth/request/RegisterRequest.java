package com.together.server.application.auth.request;

public record RegisterRequest(
        String memberId,
        String nickname,
        String password
) {
}
