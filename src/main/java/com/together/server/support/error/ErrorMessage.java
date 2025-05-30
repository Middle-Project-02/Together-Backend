package com.together.server.support.error;

import lombok.Getter;

@Getter
public class ErrorMessage {

    private final String code;
    private final String message;
    private final Object data;

    public ErrorMessage(ErrorType errorType) {
        this(errorType.name(), errorType.getMessage(), null);
    }

    public ErrorMessage(ErrorType errorType, Object data) {
        this(errorType.name(), errorType.getMessage(), data);
    }

    private ErrorMessage(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
