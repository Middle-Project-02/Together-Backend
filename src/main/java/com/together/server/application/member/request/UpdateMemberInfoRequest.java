package com.together.server.application.member.request;

public record UpdateMemberInfoRequest(
        String nickname,
        String preferredPrice,
        String fontMode
) {
}
