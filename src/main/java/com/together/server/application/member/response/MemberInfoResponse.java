package com.together.server.application.member.response;

import java.time.Instant;

public record MemberInfoResponse(
        String memberId,
        String nickname,
        boolean fontMode
) {
}
