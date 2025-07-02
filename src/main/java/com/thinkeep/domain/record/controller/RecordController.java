package com.thinkeep.domain.record.controller;

import com.thinkeep.domain.record.dto.*;
import com.thinkeep.domain.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 일기 기록 API 컨트롤러
 * JWT 토글 상태에 따라 userNo 추출 방식이 달라짐
 */
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "일기 기록", description = "일기 작성, 조회, 수정, 삭제 API")
@SecurityRequirement(name = "JWT")
public class RecordController {

    private final RecordService recordService;

    // ========================================
    // 1. 핵심 API: 일기 작성
    // ========================================

    /**
     * 오늘 일기 작성
     * POST /api/records
     *
     * JWT 비활성화: ?userNo=1 파라미터로 사용자 지정
     * JWT 활성화: Authorization 헤더에서 자동 추출
     */
    @Operation(summary = "오늘 일기 작성", description = "오늘 날짜로 새 일기를 작성합니다. JWT 비활성화 시 userNo 파라미터 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "일기 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 작성된 일기"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<?> createTodayRecord(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo,
            @RequestBody RecordCreateRequest request) {

        log.info("POST /api/records - 일기 작성 요청");

        try {
            // userNo 추출 (JWT 상태에 따라)
            Long targetUserNo = extractUserNo(authentication, userNo);

            RecordResponse response = recordService.createTodayRecord(targetUserNo, request);

            log.info("일기 작성 성공: userNo={}, recordId={}", targetUserNo, response.getRecordId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException e) {
            // 중복 작성 등 비즈니스 로직 에러
            log.warn("일기 작성 실패 (비즈니스 에러): {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));

        } catch (IllegalArgumentException e) {
            // 검증 실패 에러
            log.warn("일기 작성 실패 (검증 에러): {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));

        } catch (Exception e) {
            // 예상치 못한 에러
            log.error("일기 작성 실패 (시스템 에러)", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("일기 작성 중 오류가 발생했습니다"));
        }
    }

    // ========================================
    // 2. 상태 조회 API
    // ========================================

    /**
     * 오늘 기록 상태 조회
     * GET /api/records/today
     */
    @Operation(summary = "오늘 기록 상태 조회", description = "오늘 일기 작성 상태를 확인합니다.")
    @GetMapping("/today")
    public ResponseEntity<?> getTodayRecordStatus(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo) {

        log.info("GET /api/records/today - 오늘 기록 상태 조회");

        try {
            Long targetUserNo = extractUserNo(authentication, userNo);

            TodayRecordStatus status = recordService.getTodayRecordStatus(targetUserNo);

            log.info("오늘 기록 상태 조회 성공: userNo={}, hasRecord={}",
                    targetUserNo, status.isHasRecord());
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("오늘 기록 상태 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("상태 조회 중 오류가 발생했습니다"));
        }
    }

    /**
     * 특정 날짜 기록 조회
     * GET /api/records/{date}
     */
    @Operation(summary = "특정 날짜 일기 조회", description = "지정된 날짜의 일기를 조회합니다.")
    @GetMapping("/{date}")
    public ResponseEntity<?> getRecordByDate(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-01-15") @PathVariable String date) {

        log.info("GET /api/records/{} - 특정 날짜 기록 조회", date);

        try {
            Long targetUserNo = extractUserNo(authentication, userNo);
            LocalDate targetDate = LocalDate.parse(date);

            Optional<RecordResponse> record = recordService.getRecordByDate(targetUserNo, targetDate);

            if (record.isPresent()) {
                log.info("기록 조회 성공: userNo={}, date={}", targetUserNo, date);
                return ResponseEntity.ok(record.get());
            } else {
                log.info("기록 없음: userNo={}, date={}", targetUserNo, date);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("기록 조회 실패: date={}", date, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("기록 조회 중 오류가 발생했습니다"));
        }
    }

    // ========================================
    // 3. 수정/삭제 API
    // ========================================

    /**
     * 기록 수정
     * PUT /api/records/{recordId}
     */
    @Operation(summary = "일기 수정", description = "기존 일기를 수정합니다.")
    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateRecord(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo,
            @Parameter(description = "수정할 일기 ID") @PathVariable Long recordId,
            @RequestBody RecordCreateRequest request) {

        log.info("PUT /api/records/{} - 기록 수정", recordId);

        try {
            Long targetUserNo = extractUserNo(authentication, userNo);

            RecordResponse response = recordService.updateRecord(targetUserNo, recordId, request);

            log.info("기록 수정 성공: userNo={}, recordId={}", targetUserNo, recordId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("기록 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("기록 수정 실패: recordId={}", recordId, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("기록 수정 중 오류가 발생했습니다"));
        }
    }

    /**
     * 기록 삭제
     * DELETE /api/records/{recordId}
     */
    @Operation(summary = "일기 삭제", description = "지정된 일기를 삭제합니다.")
    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteRecord(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo,
            @Parameter(description = "삭제할 일기 ID") @PathVariable Long recordId) {

        log.info("DELETE /api/records/{} - 기록 삭제", recordId);

        try {
            Long targetUserNo = extractUserNo(authentication, userNo);

            recordService.deleteRecord(targetUserNo, recordId);

            log.info("기록 삭제 성공: userNo={}, recordId={}", targetUserNo, recordId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("기록 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("기록 삭제 실패: recordId={}", recordId, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("기록 삭제 중 오류가 발생했습니다"));
        }
    }

    // ========================================
    // 4. 유틸리티 메서드들
    // ========================================

    /**
     * JWT 상태에 따른 userNo 추출
     */
    private Long extractUserNo(Authentication authentication, Long paramUserNo) {
        if (authentication != null) {
            // JWT 활성화 상태: Spring Security에서 userNo 추출
            return (Long) authentication.getPrincipal();
        } else {
            // JWT 비활성화 상태: 파라미터에서 userNo 추출
            if (paramUserNo == null) {
                throw new IllegalArgumentException("JWT 비활성화 상태에서는 userNo 파라미터가 필요합니다");
            }
            return paramUserNo;
        }
    }

    /**
     * 에러 응답 생성
     */
    private ErrorResponse createErrorResponse(String message) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}