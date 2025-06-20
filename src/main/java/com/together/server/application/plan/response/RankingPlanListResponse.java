package com.together.server.application.plan.response;

import lombok.Getter;
import java.util.List;

/**
 * 요금제 목록 응답 클래스입니다.
 * 사용자가 선택한 연령대 기준으로 단순화
 *
 * @author ihyeeun
 * @see RankingPlanSimpleResponse
 */
@Getter
public class RankingPlanListResponse {

    private final Integer currentAgeGroup;
    private final List<RankingPlanSimpleResponse> plans;

    /**
     * 요금제 목록 응답 객체를 생성합니다.
     *
     * @param currentAgeGroup 현재 조회한 연령대 코드 (1=전체, 2=20대, 3=30대, 4=40대, 5=50대, 6=60대이상)
     * @param plans 요금제 목록
     * 사용 예시:
     * // 프론트엔드에서 숫자 코드 그대로 사용:
     * if (response.currentAgeGroup === 1) {
     *   // 전체 탭 처리
     * } else if (response.currentAgeGroup === 2) {
     *   // 20대 탭 처리
     * }
     */
    public RankingPlanListResponse(Integer currentAgeGroup, List<RankingPlanSimpleResponse> plans) {
        this.currentAgeGroup = currentAgeGroup;
        this.plans = plans;
    }
}