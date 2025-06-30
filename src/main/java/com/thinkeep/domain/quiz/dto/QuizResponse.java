package com.thinkeep.domain.quiz.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse { //퀴즈 문제를 제공하기 위한 DTO
    private Long quizId;
    private String context;
    private String question;
    private List<String> choices;
    private String answer;
}

