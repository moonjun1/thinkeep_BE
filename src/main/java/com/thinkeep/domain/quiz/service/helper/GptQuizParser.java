package com.thinkeep.domain.quiz.service.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkeep.domain.quiz.dto.QuestionSeed;
import com.thinkeep.domain.quiz.dto.QuizResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GptQuizParser {    // GPT 응답 텍스트에서 JSON 블록을 파싱

    private final ObjectMapper mapper = new ObjectMapper();

    public QuizResponse parse(String gptResponseJson, QuestionSeed seed) throws IOException {
        JsonNode root = mapper.readTree(gptResponseJson);
        String content = root.path("choices").get(0).path("message").path("content").asText();

        log.debug("[GPT 응답 - 원본 메시지 content]: \n{}", content);

        // 🔧 JSON 블록 추출 (정규식 기반)
        String jsonBlock = extractJsonBlock(content);

        if (jsonBlock == null) {
            log.error("[GPT 파싱 실패] JSON 블록 추출 실패: user={}, questionId={}, date={}, content={}",
                    seed.getRecordId(), seed.getQuestionId(), seed.getDate(), content);
            throw new IOException("GPT 응답에서 JSON 블록을 추출하지 못했습니다");
        }

        log.debug("[GPT 응답 - JSON 추출]: \n{}", jsonBlock);

        try {
            JsonNode quizJson = mapper.readTree(jsonBlock);
            String question = quizJson.get("question").asText();
            String answer = quizJson.get("answer").asText();

            // 🔧 타입 안전한 방식으로 List<String> 추출
            List<String> choices = mapper.convertValue(quizJson.get("choices"), new TypeReference<>() {});

            // 🔧 정답이 보기 안에 있는지 확인 (안전망)
            if (!choices.contains(answer)) {
                log.warn("[GPT 경고] 보기 목록에 정답이 포함되지 않음! answer={}, choices={}", answer, choices);
                choices.add(answer); // 예외 처리용으로 강제로 추가
            }

            Collections.shuffle(choices);

            log.info("[GPT 퀴즈 생성 완료] 질문: '{}', 정답: '{}', 보기: {}", question, answer, choices);

            return QuizResponse.builder()
                    .context("기록 기반 회상 퀴즈")
                    .question(question)
                    .answer(answer)
                    .choices(choices)
                    .build();

        } catch (Exception e) {
            log.error("[GPT 파싱 오류] JSON 파싱 실패. jsonBlock: {}\n에러: {}", jsonBlock, e.getMessage());
            throw new IOException("GPT 응답 JSON 파싱 실패", e);
        }
    }

    /**
     * GPT 응답 텍스트에서 첫 번째 JSON 블록만 추출
     * 🔧 개선된 정규식 (JSON 내 \n 허용)
     */
    private String extractJsonBlock(String text) {
        Pattern pattern = Pattern.compile("\\{[\\s\\S]*?\\}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }
}

