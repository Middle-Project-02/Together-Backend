package com.together.server.domain.faq;

import com.together.server.infra.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FAQChip extends BaseEntity {

    @Column(name = "question", nullable = false, length = 255)
    private String question;

    @Column(name = "answer", nullable = false, length = 1000)
    private String answer;

    public FAQChip(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}
