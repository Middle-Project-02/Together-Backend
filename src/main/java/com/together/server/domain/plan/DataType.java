package com.together.server.domain.plan;

/**
 * 요금제의 데이터 제공 방식을 나타내는 열거형입니다.
 *
 * @author ihyeeun
 */
public enum DataType {
    /**
     * 정량제 - 정해진 GB 제공 (예: 50GB, 150GB)
     */
    FIXED,

    /**
     * 무제한 - 데이터 제한 없음
     */
    UNLIMITED,

    /**
     * 일일 제공 - 매일 일정량 제공 (예: 일 5GB)
     */
    DAILY,

    /**
     * 종량제 - 사용한 만큼 과금 (예: 1KB당 0.275원)
     */
    CHARGED_PER_KB
}