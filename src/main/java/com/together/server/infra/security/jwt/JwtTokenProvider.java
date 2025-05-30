package com.together.server.infra.security.jwt;

import com.together.server.application.auth.TokenProvider;
import com.together.server.infra.security.jwt.exception.BlankTokenException;
import com.together.server.infra.security.jwt.exception.InvalidTokenException;
import com.together.server.infra.security.jwt.exception.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성, 파싱, 검증을 담당하는 컴포넌트입니다.
 * - createToken: 사용자 ID로 JWT 토큰 생성
 * - getMemberId: 토큰에서 사용자 ID 추출
 * - 내부적으로 토큰이 비었거나, 만료됐거나, 유효하지 않을 때 각각 예외 발생
 */

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;
    private final Long expirationTime;

    public JwtTokenProvider(JwtTokenProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secretKey().getBytes(StandardCharsets.UTF_8));
        this.expirationTime = properties.expirationTime();
    }

    public String createToken(String memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(memberId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public String getMemberId(String token) {
        Claims claims = toClaims(token);

        return claims.getSubject();
    }

    private Claims toClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new BlankTokenException();
        }

        try {
            Jws<Claims> claimsJws = getClaimsJws(token);

            return claimsJws.getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException(e);
        } catch (JwtException e) {
            throw new InvalidTokenException(e);
        }
    }

    private Jws<Claims> getClaimsJws(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}