package com.together.server.application.member.response;

public record UpdateMemberInfoResponse(
        String memberId,
        String nickname,
        Boolean fontMode
) {
}

