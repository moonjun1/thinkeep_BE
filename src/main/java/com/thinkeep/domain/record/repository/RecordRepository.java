package com.thinkeep.domain.record.repository;

import com.thinkeep.domain.record.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일기 기록 데이터 접근 인터페이스
 * 가장 기본적인 CRUD 메서드들만 먼저 정의
 */
@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    // === 기본 조회 메서드들 ===

    /**
     * 특정 사용자의 특정 날짜 기록 조회
     * 핵심 메서드: 하루에 하나의 기록만 가능하므로
     */
    Optional<Record> findByUserNoAndDate(Long userNo, LocalDate date);

    /**
     * 특정 사용자의 특정 날짜 기록 존재 여부 확인
     * 중복 작성 방지용
     */
    boolean existsByUserNoAndDate(Long userNo, LocalDate date);

    /**
     * 특정 사용자의 모든 기록 조회 (최신순)
     * 기록 목록 표시용
     */
    List<Record> findByUserNoOrderByDateDesc(Long userNo);

    /**
     * 특정 사용자의 특정 기간 기록 조회
     * 퀴즈 생성용 (최근 3일 기록 조회)
     *
     * 현재 사용 안함
     */
    List<Record> findByUserNoAndDateBetween(Long userNo, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 사용자의 전체 기록 개수
     * 통계용
     *
     * 현재 사용 안함
     */
    long countByUserNo(Long userNo);

    // === 권한 확인용 메서드 ===

    /**
     * 기록 ID와 사용자 번호로 기록 조회
     * 수정/삭제시 권한 확인용
     */
    Optional<Record> findByRecordIdAndUserNo(Long recordId, Long userNo);
}