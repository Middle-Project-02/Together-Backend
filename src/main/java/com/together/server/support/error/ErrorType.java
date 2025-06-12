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
    MEMBER_USEREMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다.", LogLevel.WARN),
    MEMBER_USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임입니다.", LogLevel.WARN),
    MEMBER_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.", LogLevel.WARN),
    KAKAO_USERINFO_INCOMPLETE(HttpStatus.BAD_REQUEST, "카카오 로그인 중 에러가 발생했습니다.", LogLevel.WARN),
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "카카오 토큰 요청 중 오류가 발생했습니다.", LogLevel.WARN),
    KAKAO_USERINFO_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "카카오 사용자 정보 요청 중 오류가 발생했습니다.", LogLevel.WARN),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 유요하지 않습니다.", LogLevel.WARN),
    PHONE_NUMBER_INVALID(HttpStatus.BAD_REQUEST, "전화번호 형식이 올바르지 않습니다.", LogLevel.WARN),
    REQUIRED_MEMBER_ID(HttpStatus.BAD_REQUEST, "전화번호는 필수 항목입니다.", LogLevel.WARN),
    REQUIRED_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 필수 항목입니다.", LogLevel.WARN),
    REQUIRED_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 필수 항목입니다.", LogLevel.WARN),
    ALREADY_UPDATED_FIRST_INFO(HttpStatus.BAD_REQUEST, "이미 추가 항목이 설정된 사용자입니다.", LogLevel.WARN),
    REQUIRED_AGE_GROUP(HttpStatus.BAD_REQUEST, "연령대는 필수 항목입니다.", LogLevel.WARN),
    REQUIRED_PREFERRED_PRICE(HttpStatus.BAD_REQUEST, "선호 요금제는 필수 항목입니다.", LogLevel.WARN),
    REQUIRED_FONT_MODE(HttpStatus.BAD_REQUEST, "글씨 크기 선택은 필수 항목입니다.", LogLevel.WARN);


    public static final ErrorType SOCIAL_LOGIN_ONLY = null;
    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;

    ErrorType(HttpStatus status, String message, LogLevel logLevel) {
        this.status = status;
        this.message = message;
        this.logLevel = logLevel;
    }
}