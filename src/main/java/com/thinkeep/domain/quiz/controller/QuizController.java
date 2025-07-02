package com.thinkeep.domain.quiz.controller;

import com.thinkeep.domain.quiz.dto.QuestionSeed;
import com.thinkeep.domain.quiz.dto.QuizResponse;
import com.thinkeep.domain.quiz.dto.QuizResultSummary;
import com.thinkeep.domain.quiz.dto.QuizSubmitRequest;
import com.thinkeep.domain.quiz.service.OpenAiQuizService;
import com.thinkeep.domain.quiz.service.QuizService;
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
public class QuizController {

    private final QuizService quizService;
    private final OpenAiQuizService openAiQuizService;

    /**
     * 개발 테스트용 개별 퀴즈 생성 메서드
     */
    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generateQuiz(@RequestBody QuestionSeed seed) throws IOException {
        log.info("POST /api/quizzes/generate - GPT 퀴즈 생성 요청: {}", seed);
        QuizResponse response = openAiQuizService.generateQuizFromSeed(seed);
        return ResponseEntity.ok(response);
    }

    /**
     * 오늘의 퀴즈 2개 생성
     * GET /api/quizzes/today?userNo={optional}
     */
    @GetMapping("/today")
    public ResponseEntity<List<QuizResponse>> getTodayQuizzes(
            Authentication authentication,
            @RequestParam(required = false) Long userNo // 개발용
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
    @PostMapping("/submit")
    public ResponseEntity<Void> submitQuiz(@RequestBody QuizSubmitRequest request) {
        log.info("POST /api/quizzes/submit - 퀴즈 정답 제출: quizId={}", request.getQuizId());

        quizService.submitQuizAnswer(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 오늘 푼 퀴즈 중 오답 퀴즈만 반환
     * GET /api/quizzes/today/wrong
     */
    @GetMapping("/today/wrong")
    public ResponseEntity<List<QuizResponse>> getTodayWrongQuizzes(
            Authentication authentication,
            @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        List<QuizResponse> wrongQuizzes = quizService.getTodayWrongQuizzes(resolvedUserNo);
        return ResponseEntity.ok(wrongQuizzes);
    }

    /**
     * 재시도할 퀴즈 중 1개 반환 (오답, 건너뛴 퀴즈)
     * GET /api/quizzes/today/retry-next
     */
    @GetMapping("/today/retry-next")
    public ResponseEntity<QuizResponse> getNextRetryQuiz(
            Authentication authentication,
            @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        return quizService.getNextRetryQuiz(resolvedUserNo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build()); // 더 이상 없음
    }

    /**
     * 오답 퀴즈 재시도
     * GET /api/quizzes/{quizId}/retry
     */
//    @GetMapping("/{quizId}/retry")
//    public ResponseEntity<QuizResponse> retryQuiz(@PathVariable Long quizId) {
//        log.info("GET /api/quizzes/{}/retry - 오답 퀴즈 재시도 요청", quizId);
//
//        QuizResponse retryQuiz = quizService.getRetryQuiz(quizId);
//        return ResponseEntity.ok(retryQuiz);
//    }

    /**
     * 오늘 퀴즈 결과 요약 (총 문항 수, 정답 수)
     * GET /api/quizzes/today/result
     */
    @GetMapping("/today/result")
    public ResponseEntity<QuizResultSummary> getTodayQuizResultSummary(
            Authentication authentication,
            @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        QuizResultSummary result = quizService.getTodayQuizResultSummary(resolvedUserNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 단일 퀴즈 삭제
     * DELETE /api/quizzes/{quizId}
     */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizId,
            Authentication authentication,
            @RequestParam(required = false) Long userNo
    ) {
        Long resolvedUserNo = extractUserNo(authentication, userNo);
        quizService.deleteQuiz(resolvedUserNo, quizId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 오늘 생성된 퀴즈 전체 삭제
     * DELETE /api/quizzes/today
     */
    @DeleteMapping("/today")
    public ResponseEntity<Void> deleteTodayQuizzes(
            Authentication authentication,
            @RequestParam(required = false) Long userNo
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
