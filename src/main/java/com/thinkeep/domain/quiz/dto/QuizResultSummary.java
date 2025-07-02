package com.thinkeep.domain.quiz.dto;

import lombok.Getter;

@Getter
public class QuizResultSummary { //퀴즈 결과 요약을 위한 DTO ->
    private final boolean allCorrect;
    private final int totalSolved;
    private final int correctCount;

    public QuizResultSummary(int totalSolved, int correctCount) {
        this.totalSolved = totalSolved;
        this.correctCount = correctCount;
        this.allCorrect = (totalSolved > 0 && totalSolved == correctCount);
    }
}
