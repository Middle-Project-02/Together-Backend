package com.together.server.application.member.response;

import java.time.Instant;

public record MemberInfoResponse(
        Long id,
        String email,
        String nickname,
        Instant createdAt
) {
}
