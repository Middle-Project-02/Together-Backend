package com.together.server.infra.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthentication extends AbstractAuthenticationToken {

    private final Accessor accessor;

    public static JwtAuthentication ofGuest() {
        return new JwtAuthentication(Accessor.GUEST);
    }

    public static JwtAuthentication of(String memberId) {
        return new JwtAuthentication(new Accessor(memberId));
    }

    private JwtAuthentication(Accessor accessor) {
        super(null);
        this.accessor = accessor;
        setAuthenticated(true);
    }

    @Override
    public Accessor getPrincipal() {
        return accessor;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public String getName() {
        return accessor.id();
    }
}
