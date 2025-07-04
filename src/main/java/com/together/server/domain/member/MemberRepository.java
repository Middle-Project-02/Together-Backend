package com.together.server.domain.member;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByMemberId(String memberId);

    boolean existsByMemberId(String memberId);

    List<Member> findByFcmTokenIsNotNull();
}
