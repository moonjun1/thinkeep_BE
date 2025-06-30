package com.thinkeep.domain.quiz.service;

import com.thinkeep.domain.quiz.dto.QuizResponse;
import com.thinkeep.domain.quiz.dto.QuizResultSummary;
import com.thinkeep.domain.quiz.dto.QuizSubmitRequest;
import com.thinkeep.domain.quiz.entity.Quiz;
import com.thinkeep.domain.quiz.repository.QuizRepository;
import com.thinkeep.domain.record.entity.Record;
import com.thinkeep.domain.record.repository.RecordRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    //오늘 퀴즈 생성
    @Transactional
    public List<QuizResponse> generateTodayQuizzes(Long userNo) {
        log.info("오늘 퀴즈 생성 요청: userNo={}", userNo);

        LocalDate today = LocalDate.now();

        // 최근 3일 (오늘 제외) 기록 조회
        List<Record> recentRecords = recordRepository.findByUserNoAndDateBetween(
                userNo, today.minusDays(3), today.minusDays(1)
        );

        // Q2~Q4 응답 기반 퀴즈 시드 생성
        List<QuestionSeed> seeds = extractSeedsFromRecords(recentRecords);

        // 퀴즈 2개 생성
        List<QuizResponse> quizResponses = new ArrayList<>();
        for (QuestionSeed seed : seeds.stream().limit(2).toList()) {
            QuizResponse response = generateMockQuiz(seed);

            Quiz quiz = Quiz.builder()
                    .userNo(userNo)
                    .context("기록 기반 회상 퀴즈")
                    .question(seed.getQuestion())
                    .answer(seed.getAnswer().trim())
                    .choices(String.join("||", response.getChoices()))  // 오답은 보기용
                    .submittedAt(null)
                    .isCorrect(null)
                    .build();

            quizRepository.save(quiz);
            quizResponses.add(response);
        }

        return quizResponses;
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



    //보조 메서드

    // 기록에서 Q2~Q4 기반 질문/정답 추출
    private List<QuestionSeed> extractSeedsFromRecords(List<Record> records) {
        List<QuestionSeed> seeds = new ArrayList<>();

        for (Record record : records) {
            Map<String, String> answers = record.getAnswersAsMap();

            if (answers.containsKey("Q2")) {
                seeds.add(new QuestionSeed("누구와 시간을 보냈나요?", answers.get("Q2")));
            }
            if (answers.containsKey("Q3")) {
                seeds.add(new QuestionSeed("무엇을 먹었나요?", answers.get("Q3")));
            }
            if (answers.containsKey("Q4")) {
                seeds.add(new QuestionSeed("기억에 남는 일은 무엇인가요?", answers.get("Q4")));
            }
        }
        return seeds;
    }

    // 현재는 GPT 없이 임시 3지선다 생성 (정답 + 보기용 오답 2개)
    // TODO GPT 적용 후 generateGPTQuiz()로 변경
    private QuizResponse generateMockQuiz(QuestionSeed seed) {
        List<String> choices = new ArrayList<>();
        choices.add(seed.getAnswer());  // 정답
        choices.add("모르는 보기 1");
        choices.add("모르는 보기 2");

        //정답이 가장 먼저 들어가도록 했으므로 선택 보기 섞기
        Collections.shuffle(choices);

        return QuizResponse.builder()
                .quizId(null)  // 저장 후 다시 보내줄 수도 있음
                .context("기록 기반 회상 퀴즈")
                .question(seed.getQuestion())
                .choices(choices)
                .build();
    }

    // 정답 기반 퀴즈 시드 클래스
    @Getter
    @AllArgsConstructor
    private static class QuestionSeed {
        //extractSeedsFromRecords()에서 Q2~Q4 응답을 기반으로 만들어냄
        private final String question;  //퀴즈에 쓰일 질문 텍스트
        private final String answer;    //해당 질문에 대한 사용자의 실제 기록
    }
}
