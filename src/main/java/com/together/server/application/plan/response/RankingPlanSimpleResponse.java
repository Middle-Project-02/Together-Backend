// 📁 com/together/server/application/plan/response/RankingPlanSimpleResponse.java
package com.together.server.application.plan.response;

import com.together.server.domain.plan.RankingPlan;
import lombok.Getter;

/**
 * 개별 요금제 정보를 클라이언트에게 전달하는 응답 클래스입니다.
 * @see RankingPlan
 */
@Getter
public class RankingPlanSimpleResponse {
    private final Integer id;
    private final Integer rank;
    private final String name;
    private final String regularPrice;
    private final String dataAmount;
    private final String sharedData;
    private final String speedLimit;
    private final String targetTypes;

    /**
     * RankingPlan 엔티티를 RankingPlanSimpleResponse 변환합니다.
     *
     * @param plan 변환할 RankingPlan 엔티티
     * RankingPlan plan = repository.findById(1);
     * RankingPlanSimpleResponse response = new RankingPlanSimpleResponse(plan);
     */
    public RankingPlanSimpleResponse(RankingPlan plan) {
        this.id = plan.getId();
        this.rank = plan.getRank();
        this.name = plan.getName();
        this.regularPrice = plan.getRegularPrice();
        this.dataAmount = plan.getDataAmount();
        this.sharedData = plan.getSharedData();
        this.speedLimit = plan.getSpeedLimit();
        this.targetTypes = plan.getTargetTypes();
    }
}