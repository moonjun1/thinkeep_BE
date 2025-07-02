package com.thinkeep.domain.quiz.repository;

import com.thinkeep.domain.quiz.entity.QuestionType;
import com.thinkeep.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.thinkeep.domain.record.entity.Record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * 특정 사용자의 오늘 생성된 퀴즈 전체 조회
     * 정답 개수 세기
     * 중복 퀴즈 체크
     */
    List<Quiz> findByUserNoAndSubmittedAtBetween(Long userNo, java.time.LocalDateTime start, java.time.LocalDateTime end);

    /**
     * 특정 사용자의 오답 퀴즈만 조회
     */
    List<Quiz> findByUserNoAndIsCorrectFalse(Long userNo);

    /**
     * 특정 사용자의 오늘 퀴즈 중 오답만 조회
     */
    List<Quiz> findByUserNoAndIsCorrectFalseAndSubmittedAtBetween(
            Long userNo, LocalDateTime start, LocalDateTime end
    );

    @Query("SELECT q FROM Quiz q WHERE q.userNo = :userNo " +
            "AND q.submittedAt BETWEEN :start AND :end " +
            "AND (q.isCorrect = false OR q.skipped = true)")
    List<Quiz> findTodayWrongOrSkippedQuizzes(Long userNo, LocalDateTime start, LocalDateTime end);

    /**
     * 중복 퀴즈 체크
     */
    Optional<Quiz> findByUserNoAndRecordAndQuestionId(Long userNo, Record record, QuestionType questionId);

    /**
     * 오늘 퀴즈 중 건너뛰기된 퀴즈 개수 세기
     */
    long countByUserNoAndSkippedIsTrueAndSubmittedAtBetween(
            Long userNo, LocalDateTime start, LocalDateTime end
    );



}


