package com.together.server.api;

import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import com.together.server.support.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<Void>> handleCoreException(CoreException e) {
        switch (e.getErrorType().getLogLevel()) {
            case ERROR -> log.error("CoreException : {}", e.getMessage(), e);
            case WARN -> log.warn("CoreException : {}", e.getMessage(), e);
            default -> log.info("CoreException : {}", e.getMessage(), e);
        }
        ErrorType errorType = e.getErrorType();
        ApiResponse<Void> response = ApiResponse.error(errorType.getCode(), errorType.getMessage());
        return new ResponseEntity<>(response, errorType.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Exception : {}", e.getMessage(), e);

        ErrorType errorType = ErrorType.INTERNAL_SERVER_ERROR;
        ApiResponse<Void> response = ApiResponse.error(errorType.getCode(), errorType.getMessage());
        return new ResponseEntity<>(response, errorType.getStatus());
    }
}
