package com.together.server.application.plan;

import com.together.server.application.plan.response.RankingPlanDetailResponse;
import com.together.server.application.plan.response.RankingPlanListResponse;
import com.together.server.application.plan.response.RankingPlanSimpleResponse;
import com.together.server.domain.plan.RankingPlan;
import com.together.server.domain.plan.RankingPlanRepository;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 요금제 랭킹 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자가 선택한 연령대 기준으로 요금제 조회
 *
 * @author ihyeeun
 * @see RankingPlan
 * @see RankingPlanSimpleResponse
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RankingPlanService {

    private final RankingPlanRepository rankingPlanRepository;

    /**
     * 선택된 연령대의 요금제 랭킹을 조회합니다.
     *
     * @param ageGroup 조회할 연령대 코드 (1=전체, 2=20대, 3=30대, 4=40대, 5=50대, 6=60대이상)
     * @return 요금제 랭킹 응답
     */
    public RankingPlanListResponse getRankingPlans(Integer ageGroup) {
        // 기본값 처리: ageGroup이 null이면 전체(1) 조회
        Integer targetAgeGroup = (ageGroup != null) ? ageGroup : 1;

        // 데이터베이스에서 해당 연령대 요금제 목록 조회 (20개)
        List<RankingPlan> plans = rankingPlanRepository.findByAgeGroupOrderByRank(targetAgeGroup);

        // 응답 DTO로 변환
        List<RankingPlanSimpleResponse> responseList = plans.stream()
                .map(RankingPlanSimpleResponse::new)
                .toList();

        return new RankingPlanListResponse(
                targetAgeGroup,
                responseList
        );
    }

    /**
     * 요금제 상세 정보를 조회합니다.
     *
     * @param id 요금제 ID
     * @return 요금제 상세 응답
     * @throws CoreException 요금제를 찾을 수 없는 경우
     */
    public RankingPlanDetailResponse getPlanDetail(Integer id) {
        RankingPlan plan = rankingPlanRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.PLAN_NOT_FOUND));

        return new RankingPlanDetailResponse(plan);
    }
}