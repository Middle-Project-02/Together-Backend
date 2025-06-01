package com.together.server.application.auth.response;

import java.time.Instant;

public record KakaoUserResponse (
        String kakaoId, String email, String nickname
){
}
