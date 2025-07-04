package com.together.server.infra.security.jwt.exception;

public class InvalidTokenException extends RuntimeException {

    private static final String MESSAGE = "유효하지 않은 토큰입니다.";

    public InvalidTokenException() {
        super(MESSAGE);
    }

    public InvalidTokenException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
