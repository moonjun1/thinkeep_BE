package com.thinkeep.domain.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class QuestionSeed {
    private final String questionId; // Q2, Q3, Q4
    private final String question;   // 질문 문장
    private final String answer;     // 사용자 응답
    private final LocalDate date;    // 해당 날짜
    private final Long recordId;
}