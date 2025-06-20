package com.together.server.support.error;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    // 공통
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내 예상치 못한 오류가 발생했습니다.", LogLevel.ERROR),
    UNAUTHORIZED(40001, HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", LogLevel.WARN),
    FORBIDDEN(40003, HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", LogLevel.WARN),

    // 회원
    MEMBER_NOT_FOUND(40114, HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다.", LogLevel.WARN),
    MEMBER_PHONE_NUMBER_ALREADY_EXISTS(40105, HttpStatus.BAD_REQUEST, "이미 등록된 전화번호입니다.", LogLevel.WARN),
    INVALID_PHONE_OR_PASSWORD(40113,HttpStatus.BAD_REQUEST, "전화번호 혹은 비밀번호가 일치하지 않습니다.", LogLevel.WARN),

    // 카카오
    KAKAO_USERINFO_INCOMPLETE(40110, HttpStatus.BAD_REQUEST, "카카오 로그인 중 에러가 발생했습니다.", LogLevel.WARN),
    KAKAO_TOKEN_REQUEST_FAILED(40111, HttpStatus.BAD_REQUEST, "카카오 토큰 요청 중 오류가 발생했습니다.", LogLevel.WARN),
    KAKAO_USERINFO_REQUEST_FAILED(40112, HttpStatus.BAD_REQUEST, "카카오 사용자 정보 요청 중 오류가 발생했습니다.", LogLevel.WARN),

    // 토큰
    TOKEN_NOT_IN_COOKIE(40115, HttpStatus.UNAUTHORIZED, "쿠키에 토큰이 담겨있지 않습니다.", LogLevel.WARN),
    ACCESS_TOKEN_EXPIRED(40116, HttpStatus.UNAUTHORIZED, "accessToken이 만료되었습니다.", LogLevel.WARN),
    ACCESS_TOKEN_INVALID(40117, HttpStatus.UNAUTHORIZED, "accessToken이 유효하지 않습니다.", LogLevel.WARN),
    REFRESH_TOKEN_EXPIRED(40118, HttpStatus.UNAUTHORIZED, "refreshToken이 만료되었습니다.", LogLevel.WARN),
    REFRESH_TOKEN_INVALID(40119, HttpStatus.UNAUTHORIZED, "refreshToken이 유효하지 않습니다.", LogLevel.WARN),

    // 입력값 누락
    INVALID_PHONE_NUMBER_FORMAT(40105,HttpStatus.BAD_REQUEST, "전화번호 형식이 올바르지 않습니다.", LogLevel.WARN),
    REQUIRED_MEMBER_ID(40103,HttpStatus.BAD_REQUEST, "전화번호는 필수 항목입니다.", LogLevel.WARN),
    REQUIRED_NICKNAME(40102, HttpStatus.BAD_REQUEST, "닉네임은 필수 항목입니다.", LogLevel.WARN),
    REQUIRED_PASSWORD(40104, HttpStatus.BAD_REQUEST, "비밀번호는 필수 항목입니다.", LogLevel.WARN),
    
    ALREADY_UPDATED_FIRST_INFO(40124, HttpStatus.BAD_REQUEST, "이미 추가 항목이 설정된 사용자입니다.", LogLevel.WARN),
    REQUIRED_AGE_GROUP(40121, HttpStatus.BAD_REQUEST, "연령대는 필수 항목입니다.", LogLevel.WARN),
    REQUIRED_PREFERRED_PRICE(40122, HttpStatus.BAD_REQUEST, "선호 요금제는 필수 항목입니다.", LogLevel.WARN),
    REQUIRED_FONT_MODE(40123, HttpStatus.BAD_REQUEST, "글씨 크기 선택은 필수 항목입니다.", LogLevel.WARN),
    MEMBER_WITHDRAWN(40125, HttpStatus.BAD_REQUEST, "이미 탈퇴한 사용자입니다.", LogLevel.WARN),

    // 템플릿
    TEMPLATE_NOT_FOUND(40301, HttpStatus.NOT_FOUND, "저장된 템플릿이 없습니다.", LogLevel.WARN),

    // 알림장
    NOTIFICATION_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "해당 알림장이 없습니다.", LogLevel.WARN),
    INVALID_NOTIFICATION_TITLE(40402, HttpStatus.BAD_REQUEST, "알림장 제목은 비어 있을 수 없습니다.", LogLevel.WARN),
    INVALID_NOTIFICATION_CONTENT(40403, HttpStatus.BAD_REQUEST, "알림장 세부 내용은 비어 있을 수 없습니다.", LogLevel.WARN),

    // 랭킹
    PLAN_NOT_FOUND(40501, HttpStatus.NOT_FOUND, "존재하지 않는 요금제입니다.", LogLevel.WARN),

    // 퀴즈
    QUIZ_NOT_FOUND(40601, HttpStatus.BAD_REQUEST, "해당 퀴즈를 찾을 수 없습니다.", LogLevel.WARN),
    QUIZ_EMPTY(40602, HttpStatus.BAD_REQUEST, "등록된 퀴즈가 없습니다.", LogLevel.WARN),

    // FAQ
    QUESTION_NOT_FOUND(40701, HttpStatus.NOT_FOUND, "해당하는 질문이 없습니다.", LogLevel.WARN);

    public static final ErrorType SOCIAL_LOGIN_ONLY = null;
    private final int code;
    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;

    ErrorType(int code, HttpStatus status, String message, LogLevel logLevel) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.logLevel = logLevel;
    }
}