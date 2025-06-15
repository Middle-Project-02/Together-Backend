// 📁 com/together/server/domain/plan/RankingPlanRepository.java
package com.together.server.domain.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 요금제 랭킹 데이터를 조회하는 저장소 인터페이스입니다.
 * @see RankingPlan
 */
public interface RankingPlanRepository extends JpaRepository<RankingPlan, Integer> {

    /**
     * 특정 연령대의 요금제 목록을 랭킹 순으로 조회합니다.
     *
     * @param ageGroup 조회할 연령대
     * @return 해당 연령대의 요금제 목록
     * @example
     * List<RankingPlan> plans = repository.findByAgeGroupOrderByRank("20대");
     */
    @Query("SELECT r FROM RankingPlan r WHERE r.ageGroup = :ageGroup ORDER BY r.rank ASC")
    List<RankingPlan> findByAgeGroupOrderByRank(@Param("ageGroup") String ageGroup);
}