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
    private Integer ageGroup;

    @Column(name = "preferred_price")
    private String preferredPrice;

    @Column(name = "font_mode")
    private Boolean fontMode;

    @Column(name = "is_first_login", nullable = false)
    private Boolean isFirstLogin = true;

    @Column(name = "delflag", nullable = false)
    private Boolean delflag = false;

    @Column(name = "fcm_token")
    private String fcmToken;

    public Member(String memberId, String nickname, String password) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.password = password;
        this.isFirstLogin = true;
        this.delflag = false;
        this.fontMode = false;
    }


    public void updateFirstLoginInfo(Boolean fontMode) {
        this.fontMode = fontMode;
        this.isFirstLogin = false;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePreferredPrice(String preferredPrice) {
        this.preferredPrice = preferredPrice;
    }

    public void updateFontMode(Boolean fontMode) {
        this.fontMode = fontMode;
    }

    public boolean isDeleted() {
        return delflag;
    }

    public void delete() {
        this.delflag = true;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}