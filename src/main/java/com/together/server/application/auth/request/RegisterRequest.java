package com.together.server.application.auth.request;

public record RegisterRequest(
        String email,
        String nickname,
        String password
) {
}
