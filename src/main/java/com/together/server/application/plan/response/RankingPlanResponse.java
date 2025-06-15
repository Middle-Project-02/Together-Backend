// 📁 com/together/server/application/plan/response/RankingPlanResponse.java
package com.together.server.application.plan.response;

import com.together.server.domain.plan.RankingPlan;
import lombok.Getter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * 개별 요금제 정보를 클라이언트에게 전달하는 응답 클래스입니다.
 * @see RankingPlan
 */
@Getter
public class RankingPlanResponse {

    private final Integer id;
    private final String ageGroupName;
    private final Integer rank;
    private final String name;
    private final String description;
    private final String regularPrice;
    private final String dataAmount;
    private final String sharedData;
    private final String basicBenefits;
    private final String smartDevice;
    private final String speedLimit;
    private final String targetTypes;
    private final Map<String, String> allBenefits;

    /**
     * RankingPlan 엔티티를 RankingPlanResponse로 변환합니다.
     *
     * @param plan 변환할 RankingPlan 엔티티
     * @example
     * RankingPlan plan = repository.findById(1);
     * RankingPlanResponse response = new RankingPlanResponse(plan);
     */
    public RankingPlanResponse(RankingPlan plan) {
        this.id = plan.getId();
        this.ageGroupName = plan.getAgeGroup();
        this.rank = plan.getRank();
        this.name = plan.getName();
        this.description = plan.getDescription();
        this.regularPrice = plan.getRegularPrice();
        this.dataAmount = plan.getDataAmount();
        this.sharedData = plan.getSharedData();
        this.basicBenefits = plan.getBasicBenefits();
        this.smartDevice = plan.getSmartDevice();
        this.speedLimit = plan.getSpeedLimit();
        this.targetTypes = plan.getTargetTypes();
        this.allBenefits = parseAllBenefits(plan.getAllBenefits());
    }

    private Map<String, String> parseAllBenefits(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of(); // 파싱 실패 시 빈 맵 반환
        }
    }
}