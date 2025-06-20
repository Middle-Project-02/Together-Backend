package com.together.server.api.plan;

import com.together.server.application.plan.RankingPlanService;
import com.together.server.application.plan.response.RankingPlanDetailResponse;
import com.together.server.application.plan.response.RankingPlanListResponse;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 요금제 랭킹 관련 API를 제공하는 컨트롤러입니다.
 * 사용자가 선택한 연령대 기준으로 단순화
 *
 * @author ihyeeun
 * @see RankingPlanService
 */
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
@Tag(name = "요금제 랭킹", description = "연령대별 인기 요금제 랭킹 조회 API")
public class RankingPlanController {

    private final RankingPlanService rankingPlanService;

    /**
     * 연령대별 인기 요금제 랭킹을 조회합니다.
     *
     * @param ageGroup 조회할 연령대 코드 (1=전체, 2=20대, 3=30대, 4=40대, 5=50대, 6=60대이상)
     * @return 요금제 랭킹 목록 응답
     * 사용 예시:
     * GET /api/ranking?ageGroup=2  // 20대 요금제
     * GET /api/ranking?ageGroup=1  // 전체 요금제 (기본값)
     */
    @GetMapping
    @Operation(
            summary = "인기 요금제 랭킹 조회",
            description = "연령대별 인기 요금제 20개를 조회합니다.<br>" +
                    "사용자가 선택한 연령대 탭에 따라 해당 연령대의 인기 요금제를 반환합니다.<br>" +
                    "연령대 코드: 1=전체, 2=20대, 3=30대, 4=40대, 5=50대, 6=60대이상"
    )
    public ResponseEntity<ApiResponse<RankingPlanListResponse>> getRankingPlans(
            @Parameter(description = "연령대 코드 (1=전체, 2=20대, 3=30대, 4=40대, 5=50대, 6=60대이상)", example = "2")
            @RequestParam(required = false, defaultValue = "1") Integer ageGroup) {

        RankingPlanListResponse response = rankingPlanService.getRankingPlans(ageGroup);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 요금제의 상세 정보를 조회합니다.
     *
     * @param id 요금제 ID
     * @return 요금제 상세 정보 응답
     * 사용 예시:
     * GET /api/ranking/1
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "요금제 상세 정보 조회",
            description = "해당 요금제의 상세 혜택 정보 (allBenefits)를 반환합니다."
    )
    public ResponseEntity<ApiResponse<RankingPlanDetailResponse>> getPlanDetail(
            @Parameter(description = "요금제 ID", example = "1")
            @PathVariable Integer id) {

        RankingPlanDetailResponse response = rankingPlanService.getPlanDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}