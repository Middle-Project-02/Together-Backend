package com.together.server.application.auth.response;

import java.time.Instant;

public record MemberDetailsResponse(
        String id,
        String username,
        Instant createdAt
) {
}
