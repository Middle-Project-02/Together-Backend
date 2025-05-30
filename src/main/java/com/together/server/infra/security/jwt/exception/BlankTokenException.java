package com.together.server.infra.security.jwt.exception;

public class BlankTokenException extends RuntimeException {

    private static final String MESSAGE = "토큰이 비어있습니다.";

    public BlankTokenException() {
        super(MESSAGE);
    }

    public BlankTokenException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
