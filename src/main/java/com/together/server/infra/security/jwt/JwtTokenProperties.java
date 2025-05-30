package com.together.server.infra.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtTokenProperties(
        String secretKey,
        long expirationTime
) {
}
