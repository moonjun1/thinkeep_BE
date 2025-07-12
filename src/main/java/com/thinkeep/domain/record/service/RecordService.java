package com.thinkeep.domain.record.service;

import com.thinkeep.domain.badge.dto.UserBadgeResponse;
import com.thinkeep.domain.record.dto.*;
import com.thinkeep.domain.record.entity.Record;
import com.thinkeep.domain.record.repository.RecordRepository;
import com.thinkeep.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserService userService;

    // ========================================
    // 1. 핵심 비즈니스 로직: 일기 작성
    // ========================================

    /**
     * 오늘 일기 작성
     *
     * 주요 로직:
     * 1. 오늘 이미 기록했는지 확인
     * 2. 요청 데이터 검증
     * 3. Record 엔티티 생성 및 저장
     * 4. 사용자 streak 카운트 증가
     * 5. 응답 DTO 변환
     */
    @Transactional
    public RecordCreateResponse createTodayRecord(Long userNo, RecordCreateRequest request) {
        log.info("일기 작성 시작: userNo={}, date={}, emotion={}", userNo, LocalDate.now(), request.getEmotion());

        // 1. 기본 검증
        validateCreateRequest(userNo, request);

        // 2. 오늘 이미 기록했는지 확인
        LocalDate today = LocalDate.now();
        if (recordRepository.existsByUserNoAndDate(userNo, today)) {
            throw new IllegalStateException("오늘은 이미 기록을 작성하셨습니다");
        }

        // 3. Record 엔티티 생성
        Record record = buildRecordEntity(userNo, today, request);

        // 4. 데이터베이스 저장
        Record savedRecord = recordRepository.save(record);
        log.info("일기 저장 완료: recordId={}, emotion={}", savedRecord.getRecordId(), savedRecord.getEmotion());

        // 5. 사용자 streak 카운트 증가 및 뱃지 지급 확인
        UserBadgeResponse badgeResponse = null;
        try {
            badgeResponse = userService.increaseStreakCount(userNo);
            log.info("Streak 카운트 증가 완료: userNo={}, badge={}",
                    userNo, badgeResponse != null ? badgeResponse.getBadgeId() : "없음");
        } catch (Exception e) {
            log.warn("Streak 카운트 증가 실패: {}", e.getMessage());
            // 일기 저장은 유지
        }

        // 6. 응답 DTO 변환
        return RecordCreateResponse.builder()
                .record(convertToResponse(savedRecord))
                .newBadge(badgeResponse) // null이면 뱃지 없음
                .build();
    }

    /**
     * 일기 작성 요청 검증 (감정 포함)
     */
    private void validateCreateRequest(Long userNo, RecordCreateRequest request) {
        log.debug("검증 시작: userNo={}", userNo);

        if (userNo == null || userNo <= 0) {
            log.error("검증 실패: 잘못된 사용자 번호 - userNo={}", userNo);
            throw new IllegalArgumentException("유효하지 않은 사용자 번호입니다");
        }

        if (request == null) {
            log.error("검증 실패: 요청 객체가 null");
            throw new IllegalArgumentException("일기 내용이 필요합니다");
        }

        if (!request.hasAllRequiredAnswers()) {
            log.error("검증 실패: 답변 불완전 - userNo={}, answers={}",
                    userNo, request.getAnswers() != null ? request.getAnswers().keySet() : "null");
            throw new IllegalArgumentException("모든 질문(Q1~Q4)과 감정을 입력해주세요");
        }

        // 🆕 감정 검증 추가
        if (request.getEmotion() == null || request.getEmotion().trim().isEmpty()) {
            log.error("검증 실패: 감정 누락 - userNo={}", userNo);
            throw new IllegalArgumentException("감정을 선택해주세요");
        }

        log.debug("검증 완료: userNo={}, 답변 개수={}, emotion={}",
                userNo, request.getAnswers().size(), request.getEmotion());
    }

    /**
     * Record 엔티티 생성
     */
    private Record buildRecordEntity(Long userNo, LocalDate date, RecordCreateRequest request) {
        Record record = Record.builder()
                .userNo(userNo)
                .date(date)
                .emotion(request.getEmotion()) // 감정 설정
                .build();

        // JSON 답변 설정
        record.setAnswersFromMap(request.getAnswers());

        // Q2에서 자동으로 personCategory 추출
        String q2Answer = request.getAnswers().get("Q2");
        if (q2Answer != null) {
            record.setPersonCategory(extractPersonCategory(q2Answer));
            record.setPersonName(q2Answer);
        }

        return record;
    }

    /**
     * Q2 답변에서 사람 카테고리 자동 추출
     */
    private String extractPersonCategory(String q2Answer) {
        if (q2Answer == null) return "기타";

        String answer = q2Answer.toLowerCase();

        if (answer.contains("가족") || answer.contains("엄마") || answer.contains("아빠") ||
                answer.contains("딸") || answer.contains("아들") || answer.contains("부모")) {
            return "가족";
        } else if (answer.contains("친구") || answer.contains("동기") || answer.contains("지인")) {
            return "친구";
        } else if (answer.contains("직장") || answer.contains("동료") || answer.contains("상사") ||
                answer.contains("부하") || answer.contains("회사")) {
            return "직장동료";
        } else if (answer.contains("혼자") || answer.contains("나만") || answer.contains("홀로")) {
            return "혼자";
        }

        return "기타";
    }

    // ========================================
    // 2. 오늘 기록 상태 조회
    // ========================================

    /**
     * 오늘 기록 상태 조회
     *
     * 반환 정보:
     * - 오늘 기록 존재 여부
     * - 완료 상태
     * - 적절한 액션 메시지
     */
    public TodayRecordStatus getTodayRecordStatus(Long userNo) {
        log.info("오늘 기록 상태 조회: userNo={}", userNo);

        LocalDate today = LocalDate.now();
        Optional<Record> todayRecord = recordRepository.findByUserNoAndDate(userNo, today);

        if (todayRecord.isPresent()) {
            // 오늘 기록이 있는 경우
            Record record = todayRecord.get();
            boolean isComplete = record.isComplete();

            return TodayRecordStatus.builder()
                    .hasRecord(true)
                    .date(today)
                    .record(convertToResponse(record))
                    .canCreate(false)
                    .canEdit(true)
                    .statusMessage(isComplete ? "오늘 기록을 완료했어요! 🎉" : "오늘 기록이 진행 중이에요")
                    .actionMessage(isComplete ? "회상 퀴즈를 풀어보세요!" : "기록을 마저 완성해보세요")
                    .build();
        } else {
            // 오늘 기록이 없는 경우
            return TodayRecordStatus.builder()
                    .hasRecord(false)
                    .date(today)
                    .record(null)
                    .canCreate(true)
                    .canEdit(false)
                    .statusMessage("아직 오늘 기록을 작성하지 않으셨네요")
                    .actionMessage("5분만 투자해서 오늘을 기록해보세요! ✍️")
                    .build();
        }
    }

    // ========================================
    // 3. 기본 조회 메서드들
    // ========================================

    /**
     * 특정 날짜 기록 조회
     */
    public Optional<RecordResponse> getRecordByDate(Long userNo, LocalDate date) {
        log.info("날짜별 기록 조회: userNo={}, date={}", userNo, date);

        return recordRepository.findByUserNoAndDate(userNo, date)
                .map(this::convertToResponse);
    }

    /**
     * 🆕 사용자의 모든 일기 목록 조회 (최신순)
     */
    public List<RecordResponse> getAllRecordsByUser(Long userNo) {
        log.info("사용자 전체 기록 조회: userNo={}", userNo);

        List<Record> records = recordRepository.findByUserNoOrderByDateDesc(userNo);

        log.info("조회된 기록 수: {}", records.size());

        return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 기록 수정
     */
    @Transactional
    public RecordResponse updateRecord(Long userNo, Long recordId, RecordCreateRequest request) {
        log.info("기록 수정: userNo={}, recordId={}, emotion={}", userNo, recordId, request.getEmotion());

        // 기록 조회 및 권한 확인
        Record record = recordRepository.findByRecordIdAndUserNo(recordId, userNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록을 찾을 수 없습니다"));

        // 요청 검증
        validateCreateRequest(userNo, request);

        // 데이터 업데이트 (감정 포함)
        record.setAnswersFromMap(request.getAnswers());
        record.setEmotion(request.getEmotion()); // 🆕 감정 업데이트

        // Q2 정보 업데이트
        String q2Answer = request.getAnswers().get("Q2");
        if (q2Answer != null) {
            record.setPersonCategory(extractPersonCategory(q2Answer));
            record.setPersonName(q2Answer);
        }

        Record updatedRecord = recordRepository.save(record);
        log.info("기록 수정 완료: recordId={}, emotion={}", updatedRecord.getRecordId(), updatedRecord.getEmotion());

        return convertToResponse(updatedRecord);
    }

    /**
     * 기록 삭제
     */
    @Transactional
    public void deleteRecord(Long userNo, Long recordId) {
        log.info("기록 삭제: userNo={}, recordId={}", userNo, recordId);

        // 기록 조회 및 권한 확인
        Record record = recordRepository.findByRecordIdAndUserNo(recordId, userNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록을 찾을 수 없습니다"));

        // 삭제 실행
        recordRepository.delete(record);
        log.info("기록 삭제 완료: recordId={}", recordId);
    }

    // ========================================
    // 4. 유틸리티 메서드들
    // ========================================

    /**
     * Record Entity를 RecordResponse DTO로 변환 (감정 포함)
     */
    private RecordResponse convertToResponse(Record record) {
        Map<String, String> answers = record.getAnswersAsMap();

        return RecordResponse.builder()
                .recordId(record.getRecordId())
                .userNo(record.getUserNo())
                .date(record.getDate())
                .answers(answers)
                .emotion(record.getEmotion()) // 🆕 감정 추가
                .isComplete(record.isComplete())
                .isToday(record.isToday())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .answerCount(record.getAnswerCount())
                .statusMessage(record.isComplete() ? "완료" : "진행 중")
                .build();
    }
}