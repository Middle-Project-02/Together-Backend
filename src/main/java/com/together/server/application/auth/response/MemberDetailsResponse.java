package com.together.server.application.auth.response;

import java.time.Instant;

public record MemberDetailsResponse(
        Long id,
        String memberId,
        String nickname,
        Instant createdAt
) {
}
