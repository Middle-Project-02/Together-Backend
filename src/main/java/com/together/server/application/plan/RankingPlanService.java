package com.together.server.application.plan;

import com.together.server.application.plan.response.RankingPlanDetailResponse;
import com.together.server.application.plan.response.RankingPlanListResponse;
import com.together.server.application.plan.response.RankingPlanSimpleResponse;
import com.together.server.domain.member.Member;
import com.together.server.domain.member.MemberRepository;
import com.together.server.domain.plan.RankingPlan;
import com.together.server.domain.plan.RankingPlanRepository;
import com.together.server.infra.security.Accessor;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 요금제 랭킹 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * @see RankingPlan
 * @see RankingPlanSimpleResponse
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RankingPlanService {

    private final RankingPlanRepository rankingPlanRepository;
    private final MemberRepository memberRepository;

    /**
     * 로그인 여부에 따라 연령대 기반 요금제 랭킹을 조회합니다.
     *
     * @param accessor 로그인 사용자 정보 (비로그인 시 guest 처리)
     * @param ageGroup 선택된 연령대 탭 (null이면 사용자 연령대 사용)
     * @return 요금제 랭킹 응답
     */
    public RankingPlanListResponse getRankingPlans(Accessor accessor, String ageGroup) {
        boolean isGuest = (accessor == null || accessor.isGuest());
        String userAgeGroup = null;

        if (!isGuest) {
            Member member = memberRepository.findByMemberId(accessor.id())
                    .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
            userAgeGroup = member.getAgeGroup();
        }

        String targetAgeGroup;
        if (ageGroup != null) {
            targetAgeGroup = ageGroup;
        } else if (userAgeGroup != null) {
            targetAgeGroup = userAgeGroup;
        } else {
            targetAgeGroup = "전체";
        }


        List<RankingPlan> plans = rankingPlanRepository.findByAgeGroupOrderByRank(targetAgeGroup);
        List<RankingPlanSimpleResponse> responseList = plans.stream()
                .map(RankingPlanSimpleResponse::new)
                .collect(Collectors.toList());

        return new RankingPlanListResponse(
                targetAgeGroup,
                !isGuest,
                userAgeGroup,
                responseList
        );

    }

    public RankingPlanDetailResponse getPlanDetail(Integer id) {
        RankingPlan plan = rankingPlanRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.PLAN_NOT_FOUND));
        return new RankingPlanDetailResponse(plan);
    }
}
