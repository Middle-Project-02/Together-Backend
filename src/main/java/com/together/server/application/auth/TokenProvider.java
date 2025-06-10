package com.together.server.application.auth;

public interface TokenProvider {

    String createToken(String memberId);

    String createRefreshToken(String memberId);

    String getMemberId(String token);

    boolean validateToken(String token);

}
