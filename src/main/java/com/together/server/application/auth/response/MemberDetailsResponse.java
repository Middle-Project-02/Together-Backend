package com.together.server.application.auth.response;

import java.time.Instant;

public record MemberDetailsResponse(
        String id,
        String email,
        String nickname,
        Instant createdAt
) {
}
