// 📁 com/together/server/application/plan/response/RankingPlanListResponse.java
package com.together.server.application.plan.response;

import lombok.Getter;
import java.util.List;

/**
 * 요금제 목록과 메타 정보를 클라이언트에게 전달하는 응답 클래스입니다.
 * @see RankingPlanSimpleResponse
 */
@Getter
public class RankingPlanListResponse {

    private final String currentAgeGroup;
    private final boolean isLoggedIn;
    private final String userAgeGroup;
    private final List<RankingPlanSimpleResponse> plans;

    /**
     * 요금제 목록 응답 객체를 생성합니다.
     *
     * @param currentAgeGroup 현재 조회한 연령대 (사용자가 클릭한 연령대)
     * @param isLoggedIn 사용자 로그인 여부
     * @param userAgeGroup 사용자의 연령대 (기본 탭 설정용)
     * @param plans 요금제 목록
     * List<RankingPlanSimpleResponse> planList = // 요금제 목록 생성
     * RankingPlanListResponse response = new RankingPlanListResponse(
     *     "20대", true, "20대", planList
     * );
     */
    public RankingPlanListResponse(String currentAgeGroup, boolean isLoggedIn, String userAgeGroup, List<RankingPlanSimpleResponse> plans) {
        this.currentAgeGroup = currentAgeGroup;
        this.isLoggedIn = isLoggedIn;
        this.userAgeGroup = userAgeGroup;
        this.plans = plans;
    }
}