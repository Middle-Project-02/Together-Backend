package com.together.server.domain.template;

import com.together.server.infra.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Template extends BaseEntity {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "chat_id", nullable = false)
    private Integer chatId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    public Template(Long memberId, Integer chatId, String title, String content, Integer planId) {
        this.memberId = memberId;
        this.chatId = chatId;
        this.title = title;
        this.content = content;
        this.planId = planId;
    }
}
