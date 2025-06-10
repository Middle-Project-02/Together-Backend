package com.together.server.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.together.server.infra.security.jwt.exception.InvalidTokenException;
import com.together.server.infra.web.TokenCookieHandler;
import com.together.server.support.error.ErrorType;
import com.together.server.support.response.ApiResponse;
import com.together.server.application.auth.TokenProvider;
import com.together.server.infra.security.jwt.exception.TokenExpiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final TokenCookieHandler tokenCookieHandler;
    private final TokenProvider tokenProvider;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        if (shouldAttemptTokenRefresh(exception, request)) {
            handleTokenRefresh(request, response);
            return;
        }

        handleUnauthorizedError(response);
    }

    private boolean shouldAttemptTokenRefresh(AuthenticationException exception, HttpServletRequest request) {
        return exception instanceof CredentialsExpiredException
                && tokenCookieHandler.extractRefreshToken(request).isPresent();
    }

    private void handleTokenRefresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<String> refreshTokenOpt = tokenCookieHandler.extractRefreshToken(request);
        String refreshToken = refreshTokenOpt.get();

        try {
            String memberId = tokenProvider.getMemberId(refreshToken);
            String newAccessToken = tokenProvider.createToken(memberId);
            ResponseCookie newAccessTokenCookie = tokenCookieHandler.createAccessTokenCookie(newAccessToken);

            response.setHeader(HttpHeaders.SET_COOKIE, newAccessTokenCookie.toString());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        } catch (TokenExpiredException | InvalidTokenException e) {
            handleExpiredTokens(response);
        }
    }

    private void handleExpiredTokens(HttpServletResponse response) throws IOException {
        clearAuthenticationCookies(response);
        writeErrorResponse(response);
    }

    private void handleUnauthorizedError(HttpServletResponse response) throws IOException {
        ResponseCookie expiredAccessTokenCookie = tokenCookieHandler.createExpiredAccessTokenCookie();
        response.setHeader(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString());
        writeErrorResponse(response);
    }

    private void clearAuthenticationCookies(HttpServletResponse response) {
        ResponseCookie expiredAccessTokenCookie = tokenCookieHandler.createExpiredAccessTokenCookie();
        ResponseCookie expiredRefreshTokenCookie = tokenCookieHandler.createExpiredRefreshTokenCookie();

        response.setHeader(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshTokenCookie.toString());
    }

    private void writeErrorResponse(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ApiResponse<Void> errorResponse = ApiResponse.error(ErrorType.UNAUTHORIZED);
        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(responseBody);
    }
}