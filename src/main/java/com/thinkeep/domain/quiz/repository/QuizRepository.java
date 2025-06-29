package com.thinkeep.domain.quiz.repository;

import com.thinkeep.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}


