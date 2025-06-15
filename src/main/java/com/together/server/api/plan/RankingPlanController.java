// 📁 com/together/server/api/ranking_plan/RankingPlanController.java
package com.together.server.api.plan;

import com.together.server.application.plan.RankingPlanService;
import com.together.server.application.plan.response.RankingPlanListResponse;
import com.together.server.infra.security.Accessor;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * @author ihyeeun
 * @see RankingPlanService
 */
@RestController
@RequestMapping("/api/ranking-plans")
@RequiredArgsConstructor
@Tag(name = "요금제 랭킹", description = "연령대별 인기 요금제 랭킹 조회 API")
public class RankingPlanController {

    private final RankingPlanService rankingPlanService;

    /**
     * 연령대별 인기 요금제 랭킹을 조회합니다.
     *
     * @param ageGroup 조회할 연령대 (전체, 20대, 30대, 40대, 50대, 60대이상)
     * @return 요금제 랭킹 목록 응답
     * GET /api/ranking-plans?ageGroup=20대
     * GET /api/ranking-plans (로그인 사용자의 연령대 맞춤 추천)
     */
    @GetMapping
    @Operation(
            summary = "인기 요금제 랭킹 조회",
            description = "연령대별 인기 요금제 20개를 조회합니다.<br>" +
                    "로그인이 되어있다면 사용자의 연령대에 해당하는 탭이 기본 선택되어 요금제가 조회되며,<br>" +
                    "비로그인이라면 전체 탭이 기본 선택되어 보여집니다.<br>" +
                    "[전체, 20대, 30대, 40대, 50대, 60대이상]"
    )
    public ResponseEntity<ApiResponse<RankingPlanListResponse>> getRankingPlans(@RequestParam(required = false) String ageGroup) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Accessor accessor = (principal instanceof Accessor) ? (Accessor) principal : Accessor.GUEST;

        RankingPlanListResponse response = rankingPlanService.getRankingPlans(accessor, ageGroup);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}