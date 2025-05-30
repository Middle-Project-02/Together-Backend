package com.together.server.application.auth;

public interface TokenProvider {

    String createToken(String memberId);

    String getMemberId(String token);
}
