package com.thinkeep.domain.record.controller;

import com.thinkeep.domain.record.entity.Record;
import com.thinkeep.domain.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 테스트용 컨트롤러
 * 기본 CRUD 동작 확인용 (임시)
 */
@RestController
@RequestMapping("/api/test/records")
@RequiredArgsConstructor
@Slf4j
public class TestRecordController {

    private final RecordRepository recordRepository;

    /**
     * 테스트용 기록 생성
     * GET /api/test/records/create?userNo=1
     */
    @GetMapping("/create")
    public ResponseEntity<String> createTestRecord(@RequestParam Long userNo) {
        log.info("테스트 기록 생성: userNo={}", userNo);

        try {
            // 오늘 이미 기록이 있는지 확인
            LocalDate today = LocalDate.now();
            if (recordRepository.existsByUserNoAndDate(userNo, today)) {
                return ResponseEntity.badRequest()
                        .body("오늘은 이미 기록을 작성하셨습니다");
            }

            // 테스트 기록 생성
            Record testRecord = Record.builder()
                    .userNo(userNo)
                    .date(today)
                    .answers("{\"Q1\":\"HAPPY\",\"Q2\":\"딸과 함께\",\"Q3\":\"김치찌개\",\"Q4\":\"공원 산책\"}")
                    .personCategory("가족")
                    .personName("딸")
                    .build();

            Record savedRecord = recordRepository.save(testRecord);

            String result = String.format("✅ 테스트 기록 생성 성공!\nID: %d\n날짜: %s\n사용자: %d",
                    savedRecord.getRecordId(), savedRecord.getDate(), savedRecord.getUserNo());

            log.info("테스트 기록 저장 완료: recordId={}", savedRecord.getRecordId());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("테스트 기록 생성 실패", e);
            return ResponseEntity.internalServerError()
                    .body("❌ 기록 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자별 기록 조회
     * GET /api/test/records/list?userNo=1
     */
    @GetMapping("/list")
    public ResponseEntity<List<Record>> getRecordList(@RequestParam Long userNo) {
        log.info("기록 목록 조회: userNo={}", userNo);

        try {
            List<Record> records = recordRepository.findByUserNoOrderByDateDesc(userNo);
            log.info("조회된 기록 수: {}", records.size());

            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("기록 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 오늘 기록 확인
     * GET /api/test/records/today?userNo=1
     */
    @GetMapping("/today")
    public ResponseEntity<String> getTodayRecord(@RequestParam Long userNo) {
        log.info("오늘 기록 확인: userNo={}", userNo);

        try {
            LocalDate today = LocalDate.now();
            Optional<Record> todayRecord = recordRepository.findByUserNoAndDate(userNo, today);

            if (todayRecord.isPresent()) {
                Record record = todayRecord.get();
                String result = String.format("✅ 오늘 기록이 있습니다!\nID: %d\n답변: %s",
                        record.getRecordId(), record.getAnswers());
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.ok("📝 오늘 기록이 없습니다. 새로 작성해보세요!");
            }

        } catch (Exception e) {
            log.error("오늘 기록 확인 실패", e);
            return ResponseEntity.internalServerError()
                    .body("❌ 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 전체 기록 삭제 (테스트용)
     * DELETE /api/test/records/clear?userNo=1
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllRecords(@RequestParam Long userNo) {
        log.info("전체 기록 삭제: userNo={}", userNo);

        try {
            List<Record> userRecords = recordRepository.findByUserNoOrderByDateDesc(userNo);
            recordRepository.deleteAll(userRecords);

            String result = String.format("🗑️ 사용자 %d의 기록 %d개를 모두 삭제했습니다",
                    userNo, userRecords.size());

            log.info("기록 삭제 완료: userNo={}, count={}", userNo, userRecords.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("기록 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body("❌ 삭제 실패: " + e.getMessage());
        }
    }
}