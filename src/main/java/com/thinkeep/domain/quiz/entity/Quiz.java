package com.thinkeep.domain.quiz.entity;

import com.thinkeep.domain.record.entity.Record;
import com.thinkeep.domain.user.entity.User;
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

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    // 사용자 객체 - 연관관계 탐색 시 필요할 수 있어 삽입해둠
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", insertable = false, updatable = false)
    private User user;


    private String context;              // 문제 힌트
    private String question;
    private String answer;
    private String choices;             // 3지선다형 보기 - JSON 형식 문자열

    private String userAnswer;          // 사용자가 제출한 답
    private Boolean isCorrect;

    private LocalDateTime submittedAt;  // 응답 시간

    @Column(nullable = false)
    @Builder.Default
    private Boolean skipped = false;    //사용자가 해당 퀴즈를 건너뛰었는지 여부

    @Enumerated(EnumType.STRING)
    @Column(name = "question_id", nullable = false, length = 5)
    private QuestionType questionId;

}
