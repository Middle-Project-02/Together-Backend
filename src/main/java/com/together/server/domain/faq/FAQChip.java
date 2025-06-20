package com.together.server.domain.faq;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "faq_chip")
public class FAQChip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chip_id", nullable = false)
    private Long chipId;

    @Column(name = "question", nullable = false, length = 255)
    private String question;

    @Column(name = "answer", nullable = false, length = 1000)
    private String answer;

    public FAQChip(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}
