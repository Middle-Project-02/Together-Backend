package com.together.server.application.auth.request;

public record FirstLoginRequest(
    String ageGroup,
    String preferredPrice,
    Boolean fontMode
){}
