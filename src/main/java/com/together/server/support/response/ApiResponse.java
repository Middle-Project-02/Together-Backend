package com.together.server.support.response;

import com.together.server.support.error.ErrorMessage;
import com.together.server.support.error.ErrorType;
import java.time.Instant;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final int code;
    private final T content;
    private final String message;

    public ApiResponse(int code, T content, String message) {
        this.code = code;
        this.content = content;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T content) {
        return new ApiResponse<>(20000, content, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }
}