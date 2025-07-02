package com.thinkeep.domain.quiz.service;

import com.thinkeep.domain.quiz.dto.*;
import com.thinkeep.domain.quiz.entity.QuestionType;
import com.thinkeep.domain.quiz.entity.Quiz;
import com.thinkeep.domain.quiz.repository.QuizRepository;
import com.thinkeep.domain.record.entity.Record;
import com.thinkeep.domain.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final RecordRepository recordRepository;
    private final OpenAiQuizService openAiService;

    //오늘 퀴즈 생성
    @Transactional
    public List<QuizResponse> generateTodayQuizzes(Long userNo) {
        log.info("오늘 퀴즈 생성 요청: userNo={}", userNo);

        LocalDate today = LocalDate.now();
        List<Record> recentRecords = recordRepository.findByUserNoAndDateBetween(
                userNo, today.minusDays(3), today.minusDays(1)
        );

        List<QuestionSeed> seeds = extractSeedsFromRecords(recentRecords);
        List<QuizResponse> quizResponses = new ArrayList<>();

        int createdCount = 0;

        for (QuestionSeed seed : seeds) {
            if (createdCount >= 2) break;

            // 1. Record 객체 생성
            Record record = Record.builder().recordId(seed.getRecordId()).build();

            // 2. 중복 퀴즈 존재 여부 확인
            boolean alreadyExists = quizRepository
                    .findByUserNoAndRecordAndQuestionId(
                            userNo,
                            record,
                            QuestionType.valueOf(seed.getQuestionId())
                    ).isPresent();

            if (alreadyExists) {
                log.info("[중복 퀴즈 건너뜀] userNo={}, recordId={}, questionId={}",
                        userNo, seed.getRecordId(), seed.getQuestionId());
                continue;
            }

            // 3. GPT 기반 퀴즈 생성
            QuizResponse response = generateGptQuiz(seed);

            // 4. 퀴즈 저장
            Quiz quiz = Quiz.builder()
                    .userNo(userNo)
                    .record(record)
                    .questionId(QuestionType.valueOf(seed.getQuestionId()))
                    .context("기록 기반 회상 퀴즈")
                    .question(response.getQuestion())
                    .answer(response.getAnswer())
                    .choices(String.join("||", response.getChoices()))
                    .submittedAt(null)
                    .isCorrect(null)
                    .skipped(false)
                    .build();

            // 저장 후 실제 quizId 획득
            Quiz savedQuiz = quizRepository.save(quiz);
            response.setQuizId(savedQuiz.getQuizId());

            quizResponses.add(response);
            createdCount++;
        }

        return quizResponses;
    }

    // 건너뛰기 체크(하루 2번) -> 하루에 건너뛰기한 퀴즈가 2개 미만인 경우만 true 반환
    public boolean isSkipAllowedToday(Long userNo) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        long skippedCount = quizRepository.countByUserNoAndSkippedIsTrueAndSubmittedAtBetween(
                userNo, todayStart, tomorrowStart
        );

        return skippedCount < 2;
    }

    //오늘 스킵 횟수 및 남은 가능 횟수 조회
    public SkipStatusResponse getTodaySkipStatus(Long userNo) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        long skipped = quizRepository.countByUserNoAndSkippedIsTrueAndSubmittedAtBetween(
                userNo, todayStart, tomorrowStart
        );

        int skippedCount = (int) skipped;
        int maxSkip = 2;
        int remaining = Math.max(0, maxSkip - skippedCount);

        return new SkipStatusResponse(skippedCount, remaining);
    }

    // 퀴즈 정답 제출
    @Transactional
    public void submitQuizAnswer(QuizSubmitRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("퀴즈가 존재하지 않습니다"));

        String correctAnswer = quiz.getAnswer();
        String submitted = request.getUserAnswer();
        Boolean skipped = Boolean.TRUE.equals(request.getSkipped());

        if (skipped) {
            if (!isSkipAllowedToday(quiz.getUserNo())) {
                throw new IllegalStateException("오늘은 더 이상 퀴즈를 건너뛸 수 없습니다 (하루 최대 2회)");
            }
            // 건너뛰기 처리
            quiz.setUserAnswer(null);
            quiz.setIsCorrect(false);  // 건너뛴 경우 정답 처리 안됨
            quiz.setSkipped(true);
        } else {
            // 일반 제출 처리
            boolean isCorrect = correctAnswer != null && submitted != null &&
                    correctAnswer.trim().equalsIgnoreCase(submitted.trim());

            quiz.setUserAnswer(submitted);
            quiz.setIsCorrect(isCorrect);
            quiz.setSkipped(false);
        }

        quiz.setSubmittedAt(LocalDateTime.now());
        quizRepository.save(quiz);

        log.info("퀴즈 제출 처리 완료: quizId={}, skipped={}, isCorrect={}, userAnswer='{}'",
                quiz.getQuizId(), skipped, quiz.getIsCorrect(), quiz.getUserAnswer());
    }

    //오늘 푼 퀴즈 중 오답 전체 목록 조회
    public List<QuizResponse> getTodayWrongQuizzes(Long userNo) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        List<Quiz> wrongOrSkipped = quizRepository.findTodayWrongOrSkippedQuizzes(userNo, todayStart, tomorrowStart);

        return wrongOrSkipped.stream()
                .map(q -> QuizResponse.builder()
                        .quizId(q.getQuizId())
                        .context(q.getContext())
                        .question(q.getQuestion())
                        .choices(Arrays.asList(q.getChoices().split("\\|\\|")))
                        .build())
                .toList();
    }

    // 오답 퀴즈 재시도 -> 사용자가 오답 퀴즈를 직접 선택해서 다시 풀 수 있음
//    public QuizResponse getRetryQuiz(Long quizId) {
//        Quiz quiz = quizRepository.findById(quizId)
//                .orElseThrow(() -> new IllegalArgumentException("퀴즈가 존재하지 않습니다"));
//
//        return QuizResponse.builder()
//                .quizId(quiz.getQuizId())
//                .context(quiz.getContext())
//                .question(quiz.getQuestion())
//                .choices(Arrays.asList(quiz.getChoices().split("\\|\\|")))
//                .build();
//    }

    // 오답 퀴즈 재시도 -> 오답/스킵된 문제의 순차 재시도를 서버가 관리
    public Optional<QuizResponse> getNextRetryQuiz(Long userNo) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        List<Quiz> wrongOrSkipped = quizRepository.findTodayWrongOrSkippedQuizzes(userNo, todayStart, tomorrowStart);

        return wrongOrSkipped.stream()
                .findFirst()
                .map(q -> QuizResponse.builder()
                        .quizId(q.getQuizId())
                        .context(q.getContext())
                        .question(q.getQuestion())
                        .choices(Arrays.asList(q.getChoices().split("\\|\\|")))
                        .build());
    }


    //오늘 퀴즈 결과 요약 반환 (총 문항 수, 맞춘 문항 수)
    public QuizResultSummary getTodayQuizResultSummary(Long userNo) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        List<Quiz> todayQuizzes = quizRepository.findByUserNoAndSubmittedAtBetween(userNo, todayStart, tomorrowStart);
        long total = todayQuizzes.size();
        long correct = todayQuizzes.stream().filter(Quiz::getIsCorrect).count();

        return new QuizResultSummary((int) total, (int) correct);
    }

    //단일 퀴즈 삭제 -> 사용자가 자신의 퀴즈 삭제
    @Transactional
    public void deleteQuiz(Long userNo, Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈입니다."));

        // 본인 소유 퀴즈인지 확인
        if (!quiz.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        quizRepository.delete(quiz);
        log.info("퀴즈 삭제 완료: quizId={}, userNo={}", quizId, userNo);
    }

    //생성일 기준 해당 사용자의 퀴즈를 모두 삭제
    @Transactional
    public void deleteTodayQuizzes(Long userNo) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        List<Quiz> todayQuizzes = quizRepository.findByUserNoAndSubmittedAtBetween(userNo, todayStart, tomorrowStart);
        quizRepository.deleteAll(todayQuizzes);

        log.info("오늘 퀴즈 전체 삭제 완료: userNo={}, 삭제된 수={}", userNo, todayQuizzes.size());
    }




    //보조 메서드

    // 기록에서 Q2~Q4 기반 질문/정답 추출
    private List<QuestionSeed> extractSeedsFromRecords(List<Record> records) {
        List<QuestionSeed> seeds = new ArrayList<>();

        for (Record record : records) {
            Map<String, String> answers = record.getAnswersAsMap();

            if (answers.containsKey("Q2")) {
                seeds.add(new QuestionSeed("Q2", "누구와 시간을 보냈나요?", answers.get("Q2"), record.getDate(), record.getRecordId()));
            }
            if (answers.containsKey("Q3")) {
                seeds.add(new QuestionSeed("Q3", "무엇을 먹었나요?", answers.get("Q3"), record.getDate(), record.getRecordId()));
            }
            if (answers.containsKey("Q4")) {
                seeds.add(new QuestionSeed("Q4", "기억에 남는 일은 무엇인가요?", answers.get("Q4"), record.getDate(), record.getRecordId()));
            }
        }
        return seeds;
    }

    // 현재는 GPT 없이 임시 3지선다 생성 (정답 + 보기용 오답 2개)
    private QuizResponse generateGptQuiz(QuestionSeed seed) {
        try {
            return openAiService.generateQuizFromSeed(seed);
        } catch (IOException e) {
            log.error("GPT 퀴즈 생성 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("GPT 퀴즈 생성 실패", e);
        }
    }


}
