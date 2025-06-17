package com.together.server.domain.quiz;

import com.together.server.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MemberScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private int totalScore = 0;

    public MemberScore(Member member) {
        this.member = member;
        this.totalScore = 0;
    }

    public void increaseScore(int point) {
        this.totalScore += point;
    }
}