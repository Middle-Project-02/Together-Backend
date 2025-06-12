package com.together.server.application.member.response;

import java.time.Instant;

public record UpdateMemberInfoResponse(
        String memberId,
        String nickname,
        String preferredPrice,
        String fontMode,
        Instant updated_at
) {
}
