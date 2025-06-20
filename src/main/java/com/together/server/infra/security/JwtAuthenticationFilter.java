package com.together.server.infra.security;

import com.together.server.application.auth.AuthService;
import com.together.server.application.auth.TokenProvider;
import com.together.server.application.auth.exception.MemberNotFoundException;
import com.together.server.application.auth.response.MemberDetailsResponse;
import com.together.server.infra.security.jwt.exception.BlankTokenException;
import com.together.server.infra.security.jwt.exception.InvalidTokenException;
import com.together.server.infra.security.jwt.exception.TokenExpiredException;
import com.together.server.infra.web.TokenCookieHandler;
import com.together.server.support.error.ErrorType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final TokenCookieHandler tokenCookieHandler;
    private final AuthService authService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            Optional<String> tokenOpt = tokenCookieHandler.extractAccessToken(request);
            tokenOpt.ifPresent(this::handleToken);

            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, e);
        }
    }

    private void handleToken(String token) {
        String memberId = extractMemberId(token);
        MemberDetailsResponse memberInfo = fetchMemberInfo(memberId);

        Authentication authentication = JwtAuthentication.of(String.valueOf(memberInfo.id()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractMemberId(String token) {
        try {
            return tokenProvider.getMemberId(token);
        } catch (BlankTokenException e) {
            throw new BadCredentialsException(ErrorType.TOKEN_NOT_IN_COOKIE.name(), e);
        } catch (TokenExpiredException e) {
            throw new CredentialsExpiredException(ErrorType.ACCESS_TOKEN_EXPIRED.name(), e);
        } catch (InvalidTokenException e) {
            throw new BadCredentialsException(ErrorType.ACCESS_TOKEN_INVALID.name(), e);
        }
    }

    private MemberDetailsResponse fetchMemberInfo(String memberId) {
        try {
            return authService.getMemberDetails(memberId);
        } catch (MemberNotFoundException e) {
            throw new BadCredentialsException(ErrorType.MEMBER_NOT_FOUND.name(), e);
        }
    }
}
