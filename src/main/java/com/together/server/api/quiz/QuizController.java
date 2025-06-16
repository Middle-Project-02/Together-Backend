package com.together.server.api.quiz;

import com.together.server.application.quiz.QuizService;
import com.together.server.domain.quiz.Quiz;
import com.together.server.infra.security.Accessor;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/random")
    public ResponseEntity<Quiz> getRandomQuiz() {
        return ResponseEntity.ok(quizService.getRandomQuiz());
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswer(
            @RequestParam Long quizId,
            @RequestParam boolean userAnswer,
            @AuthenticationPrincipal Accessor accessor
    ) {
        if (accessor.isGuest()) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        Long memberId = Long.valueOf(accessor.id());
        return ResponseEntity.ok(quizService.submitAnswer(quizId, memberId, userAnswer));
    }

    @GetMapping("/score")
    public ResponseEntity<?> getScore(@AuthenticationPrincipal Accessor accessor) {
        if (accessor.isGuest()) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }

        Long memberId = Long.valueOf(accessor.id());
        return ResponseEntity.ok(Map.of("score", quizService.getTotalScore(memberId)));
    }
}

