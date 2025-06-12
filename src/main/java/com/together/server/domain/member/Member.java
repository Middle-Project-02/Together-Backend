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

    @Column(name = "age_group")
    private String ageGroup;

    @Column(name = "preferred_price")
    private String preferredPrice;

    @Column(name = "font_mode")
    private String fontMode;

    @Column(name = "is_first_login", nullable = false)
    private Boolean isFirstLogin = true;

    public Member(String memberId, String nickname, String password) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.password = password;
        this.isFirstLogin = true;
    }

    public void updateFirstLoginInfo(String ageGroup, String preferredPrice, Boolean fontMode) {
        this.ageGroup = ageGroup;
        this.preferredPrice = preferredPrice;
        this.fontMode = String.valueOf(fontMode);
        this.isFirstLogin = false;
    }
}