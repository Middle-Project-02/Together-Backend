package com.together.server.domain.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberScoreRepository extends JpaRepository<MemberScore, Long> {
    Optional<MemberScore> findByMemberId(Long memberId);
}
