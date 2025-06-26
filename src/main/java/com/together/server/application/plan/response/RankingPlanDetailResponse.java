package com.together.server.application.plan.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.together.server.domain.plan.DataType;
import com.together.server.domain.plan.RankingPlan;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 요금제 상세 정보를 클라이언트에게 전달하는 응답 클래스입니다.
 * allBenefits를 JSON 객체로 파싱해서 전달
 *
 * @author ihyeeun
 * @see RankingPlan
 */
@Getter
@Slf4j
public class RankingPlanDetailResponse {

    private final Integer rank;
    private final String name;
    private final String description;
    private final String regularPrice;
    private final String dataAmount;
    private final String speedLimit;
    private final Integer dataAmountGb;
    private final DataType dataType;
    private final Map<String, Object> allBenefits;

    public RankingPlanDetailResponse(RankingPlan plan) {
        this.rank = plan.getRank();
        this.name = plan.getName();
        this.description = plan.getDescription();
        this.regularPrice = plan.getRegularPrice();
        this.dataAmount = plan.getDataAmount();
        this.speedLimit = plan.getSpeedLimit();
        this.dataAmountGb = plan.getDataAmountGb();
        this.dataType = plan.getDataType();
        this.allBenefits = parseAllBenefits(plan.getAllBenefits());
    }

    /**
     * allBenefits JSON 문자열을 Map 객체로 파싱합니다.
     *
     * @param allBenefitsJson JSON 문자열
     * @return 파싱된 Map 객체, 실패 시 빈 Map
     */
    private Map<String, Object> parseAllBenefits(String allBenefitsJson) {
        if (allBenefitsJson == null || allBenefitsJson.trim().isEmpty()) {
            return Map.of(); // 빈 Map 반환
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(allBenefitsJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("allBenefits JSON 파싱 실패 - planName: {}, error: {}", name, e.getMessage());
            return Map.of(); // 파싱 실패 시 빈 Map 반환
        }
    }
}