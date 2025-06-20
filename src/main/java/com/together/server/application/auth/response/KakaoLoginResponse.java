package com.together.server.application.auth.response;

public record KakaoLoginResponse (
        String accessToken,
        String refreshToken,
        KakaoUserResponse user
){
}
