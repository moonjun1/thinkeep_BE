package com.thinkeep.domain.quiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequest {
    private Long quizId;
    private String userAnswer;
}

