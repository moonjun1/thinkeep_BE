package com.thinkeep.domain.quiz.entity;

import com.thinkeep.domain.record.entity.Record;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private Record record;

    @Column(nullable = false)
    private Long userNo;

    private String context;              // 문제 힌트
    private String question;
    private String answer;
    private String choices;             // 3지선다형 보기 - JSON 형식 문자열

    private String userAnswer;          // 사용자가 제출한 답
    private Boolean isCorrect;

    private LocalDateTime submittedAt;  // 응답 시간
}
