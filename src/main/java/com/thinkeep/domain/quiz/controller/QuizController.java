package com.thinkeep.domain.quiz.controller;

import com.thinkeep.domain.quiz.dto.*;
import com.thinkeep.domain.quiz.service.OpenAiQuizService;
import com.thinkeep.domain.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "퀴즈", description = "일기 기반 회상 퀴즈 API")
@SecurityRequirement(name = "JWT")
public class QuizController {

    private final QuizService quizService;
    private final OpenAiQuizService openAiQuizService;

    /**
     * 개발 테스트용 개별 퀴즈 생성 메서드
     * POST /api/quizzes/generate
     */
    @Operation(
            summary = "개별 퀴즈 생성 (개발용)",
            description = "특정 질문 시드를 기반으로 GPT를 이용해 퀴즈를 생성합니다. 개발 및 테스트용입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "퀴즈 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 질문 시드 데이터"),
            @ApiResponse(responseCode = "500", description = "GPT API 호출 실패")
    })
    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generateQuiz(@RequestBody QuestionSeed seed) throws IOException {
        log.info("POST /api/quizzes/generate - GPT 퀴즈 생성 요청: {}", seed);
        QuizResponse response = openAiQuizService.generateQuizFromSeed(seed);
        return ResponseEntity.ok(response);
    }

    /**
     * 오늘의 퀴즈 2개 생성
     * GET /api/quizzes/today
     */
    @Operation(
            summary = "오늘의 퀴즈 생성",
            description = "최근 3일간의 일기 기록을 바탕으로 오늘의 회상 퀴즈 2개를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "퀴즈 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "퀴즈 생성을 위한 충분한 기록이 없음")
    })
    @GetMapping("/today")
    public ResponseEntity<List<QuizResponse>> getTodayQuizzes(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        log.info("GET /api/quizzes/today - userNo={}", resolvedUserNo);

        List<QuizResponse> quizzes = quizService.generateTodayQuizzes(resolvedUserNo);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 정답 제출
     * POST /api/quizzes/submit
     */
    @Operation(
            summary = "퀴즈 정답 제출",
            description = "퀴즈의 정답을 제출하거나 건너뛰기를 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정답 제출 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 건너뛰기 횟수 초과"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 퀴즈")
    })
    @PostMapping("/submit")
    public ResponseEntity<Void> submitQuiz(@RequestBody QuizSubmitRequest request) {
        log.info("POST /api/quizzes/submit - 퀴즈 정답 제출: quizId={}", request.getQuizId());

        quizService.submitQuizAnswer(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 오늘 사용자가 건너뛴 횟수 및 남은 스킵 가능 횟수 조회
     * GET /api/quizzes/today/skip-status
     */
    @Operation(
            summary = "오늘 스킵 상태 조회",
            description = "오늘 건너뛴 퀴즈 횟수와 남은 건너뛰기 가능 횟수를 조회합니다. (하루 최대 2회)"
    )
    @ApiResponse(responseCode = "200", description = "스킵 상태 조회 성공")
    @GetMapping("/today/skip-status")
    public ResponseEntity<SkipStatusResponse> getTodaySkipStatus(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        SkipStatusResponse response = quizService.getTodaySkipStatus(resolvedUserNo);
        return ResponseEntity.ok(response);
    }

    /**
     * 오늘 푼 퀴즈 중 오답 퀴즈만 반환
     * GET /api/quizzes/today/wrong
     */
    @Operation(
            summary = "오늘 오답 퀴즈 조회",
            description = "오늘 풀었던 퀴즈 중 틀렸거나 건너뛴 퀴즈들을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "오답 퀴즈 조회 성공"),
            @ApiResponse(responseCode = "204", description = "오답 퀴즈가 없음")
    })
    @GetMapping("/today/wrong")
    public ResponseEntity<List<QuizResponse>> getTodayWrongQuizzes(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        List<QuizResponse> wrongQuizzes = quizService.getTodayWrongQuizzes(resolvedUserNo);
        return ResponseEntity.ok(wrongQuizzes);
    }

    /**
     * 재시도할 퀴즈 중 1개 반환 (오답, 건너뛴 퀴즈)
     * GET /api/quizzes/today/retry-next
     */
    @Operation(
            summary = "다음 재시도 퀴즈 조회",
            description = "오답이거나 건너뛴 퀴즈 중 다음으로 재시도할 퀴즈 1개를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재시도 퀴즈 반환"),
            @ApiResponse(responseCode = "204", description = "더 이상 재시도할 퀴즈가 없음")
    })
    @GetMapping("/today/retry-next")
    public ResponseEntity<QuizResponse> getNextRetryQuiz(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        return quizService.getNextRetryQuiz(resolvedUserNo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build()); // 더 이상 없음
    }

    /**
     * 오늘 퀴즈 결과 요약 (총 문항 수, 정답 수)
     * GET /api/quizzes/today/result
     */
    @Operation(
            summary = "오늘 퀴즈 결과 요약",
            description = "오늘 풀었던 퀴즈의 총 문항 수와 정답 수를 요약해서 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "퀴즈 결과 요약 반환")
    @GetMapping("/today/result")
    public ResponseEntity<QuizResultSummary> getTodayQuizResultSummary(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        QuizResultSummary result = quizService.getTodayQuizResultSummary(resolvedUserNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 단일 퀴즈 삭제
     * DELETE /api/quizzes/{quizId}
     */
    @Operation(
            summary = "단일 퀴즈 삭제",
            description = "특정 퀴즈를 삭제합니다. 본인이 생성한 퀴즈만 삭제 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "퀴즈 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 퀴즈")
    })
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @Parameter(description = "삭제할 퀴즈 ID") @PathVariable Long quizId,
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        quizService.deleteQuiz(resolvedUserNo, quizId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 오늘 생성된 퀴즈 전체 삭제
     * DELETE /api/quizzes/today
     */
    @Operation(
            summary = "오늘 퀴즈 전체 삭제",
            description = "오늘 생성된 모든 퀴즈를 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "오늘 퀴즈 전체 삭제 성공")
    @DeleteMapping("/today")
    public ResponseEntity<Void> deleteTodayQuizzes(
            Authentication authentication,
            @Parameter(description = "사용자 번호 (JWT 비활성화 시 필수)") @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        quizService.deleteTodayQuizzes(resolvedUserNo);
        return ResponseEntity.noContent().build();
    }

    /**
     * JWT 인증 활성/비활성 모드에 따라 userNo 추출
     * - JWT ON: authentication에서 추출
     * - JWT OFF: userNo 파라미터
     */
    private Long extractUserNo(Authentication authentication, Long userNo) {
        if (authentication != null) {
            try {
                return (Long) authentication.getPrincipal();
            } catch (Exception e) {
                throw new IllegalArgumentException("인증 정보가 올바르지 않습니다");
            }
        } else {
            if (userNo == null) {
                log.error("JWT OFF 상태에서 userNo 파라미터 누락됨");
                throw new IllegalArgumentException("userNo 파라미터가 필요합니다 (JWT OFF)");
            }
            return userNo;
        }
    }
}