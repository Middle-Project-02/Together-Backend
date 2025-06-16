package com.together.server.application.plan.response;

import com.together.server.domain.plan.RankingPlan;
import lombok.Getter;

@Getter
public class RankingPlanDetailResponse {
    private final String allBenefits;

    public RankingPlanDetailResponse(RankingPlan plan) {
        this.allBenefits = plan.getAllBenefits();
    }
}
