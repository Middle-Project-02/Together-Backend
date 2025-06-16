package com.together.server.domain.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean correctAnswer; // true = 스미싱 문자

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanationIfCorrect;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanationIfWrong;

    // 생성자
    public Quiz(String message, boolean correctAnswer, String explanationIfCorrect, String explanationIfWrong) {
        this.message = message;
        this.correctAnswer = correctAnswer;
        this.explanationIfCorrect = explanationIfCorrect;
        this.explanationIfWrong = explanationIfWrong;
    }
}

