package com.together.server.domain.member;

import com.together.server.infra.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends BaseEntity {

    @Column(name = "member_id", nullable = false, unique = true)
    private String memberId;

    @Column(name = "member_password", nullable = false)
    private String password;

    @Column(name = "member_nickname", nullable = false)
    private String nickname;

    public Member(String memberId, String nickname, String password) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.password = password;
    }
}
