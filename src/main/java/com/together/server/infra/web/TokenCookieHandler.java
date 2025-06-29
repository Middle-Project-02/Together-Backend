package com.together.server.infra.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

/**
 * JWT 토큰을 쿠키로 생성/삭제/추출하는 역할을 담당하는 컴포넌트입니다.
 * - createAccessTokenCookie: JWT 토큰을 담은 쿠키 생성
 * - createExpiredAccessTokenCookie: 만료된(삭제용) 쿠키 생성
 * - extractAccessToken: 요청에서 JWT 토큰 쿠키 추출
 */

@Component
@RequiredArgsConstructor
public class TokenCookieHandler {

    private final TokenCookieProperties properties;

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(properties.key(), token)
                .httpOnly(properties.httpOnly())
                .secure(properties.secure())
                .path(properties.path())
                .sameSite("None")
                .maxAge(properties.maxAge())
                .build();
    }

    public ResponseCookie createExpiredAccessTokenCookie() {
        return ResponseCookie.from(properties.key(), "")
                .httpOnly(properties.httpOnly())
                .secure(properties.secure())
                .path(properties.path())
                .sameSite("None")
                .maxAge(0)
                .build();
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        String tokenCookieName = properties.key();
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> tokenCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        String refreshKey = properties.refreshKey();

        if (refreshKey == null || refreshKey.isEmpty()) {
            throw new IllegalArgumentException("refreshKey must not be empty");
        }

        return ResponseCookie.from(properties.refreshKey(), refreshToken)
                .httpOnly(properties.httpOnly())
                .secure(properties.secure())
                .path(properties.path())
                .sameSite("None")
                .maxAge(properties.refreshMaxAge())
                .build();
    }

    public ResponseCookie createExpiredRefreshTokenCookie() {
        return ResponseCookie.from(properties.refreshKey(), "")
                .httpOnly(properties.httpOnly())
                .secure(properties.secure())
                .path(properties.path())
                .sameSite("None")
                .maxAge(0)
                .build();
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, properties.refreshKey()))
                .map(Cookie::getValue);
    }


}
