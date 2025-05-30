package com.together.server.support.error;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내 예상치 못한 오류가 발생했습니다.", LogLevel.ERROR),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", LogLevel.WARN),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", LogLevel.WARN),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다.", LogLevel.WARN),
    MEMBER_USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 사용자 이름입니다.", LogLevel.WARN),
    MEMBER_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.", LogLevel.WARN),
    ;

    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;

    ErrorType(HttpStatus status, String message, LogLevel logLevel) {
        this.status = status;
        this.message = message;
        this.logLevel = logLevel;
    }
}