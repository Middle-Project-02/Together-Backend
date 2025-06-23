package com.together.server.application.auth.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean isFirstLogin,
        boolean fontMode
) {
}