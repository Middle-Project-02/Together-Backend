package com.together.server.application.auth.response;

public record KakaoUserResponse (
        String kakaoId, String email, String nickname
){
}
