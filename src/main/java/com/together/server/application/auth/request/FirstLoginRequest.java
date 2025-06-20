package com.together.server.application.auth.request;

public record FirstLoginRequest(
    Integer ageGroup,
    String preferredPrice,
    Boolean fontMode
){}
