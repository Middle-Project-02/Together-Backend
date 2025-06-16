package com.together.server.application.quiz;

import com.together.server.domain.member.Member;
import com.together.server.domain.member.MemberRepository;
import com.together.server.domain.quiz.MemberScore;
import com.together.server.domain.quiz.MemberScoreRepository;
import com.together.server.domain.quiz.Quiz;
import com.together.server.domain.quiz.QuizRepository;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final MemberScoreRepository scoreRepository;
    private final MemberRepository memberRepository;

    public Quiz getRandomQuiz() {
        List<Quiz> all = quizRepository.findAll();
        if (all.isEmpty()) throw new CoreException(ErrorType.QUIZ_EMPTY);
        return all.get(new Random().nextInt(all.size()));
    }

    public Map<String, Object> submitAnswer(Long quizId, Long memberId, boolean userAnswer) {
        Member member = memberRepository.findById(String.valueOf(memberId))
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new CoreException(ErrorType.QUIZ_NOT_FOUND));

        boolean isCorrect = (quiz.isCorrectAnswer() == userAnswer);
        String feedback = isCorrect ? quiz.getExplanationIfCorrect() : quiz.getExplanationIfWrong();

        MemberScore score = scoreRepository.findByMemberId(memberId)
                .orElseGet(() -> scoreRepository.save(new MemberScore(member)));

        if (isCorrect) {
            score.increaseScore(1);
            scoreRepository.save(score);
        }

        return Map.of(
                "isCorrect", isCorrect,
                "message", feedback,
                "totalScore", score.getTotalScore()
        );
    }

    public int getTotalScore(Long memberId) {
        Member member = memberRepository.findById(String.valueOf(memberId))
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        return scoreRepository.findByMemberId(memberId)
                .map(MemberScore::getTotalScore)
                .orElse(0);
    }

}

