package com.thinkeep.domain.quiz.service.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkeep.domain.quiz.dto.QuestionSeed;
import com.thinkeep.domain.quiz.dto.QuizResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class GptQuizParser {    //GPT가 주는 비정형 응답(텍스트 + JSON)을 파싱 : 정규식 기반 JSON 추출

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * GPT 응답 JSON을 파싱하여 QuizResponse로 변환
     */
    public QuizResponse parse(String gptResponseJson, QuestionSeed seed) throws IOException {
        JsonNode root = mapper.readTree(gptResponseJson);   // GPT 응답 JSON 전체에서 content 부분 추출
        String content = root.path("choices").get(0).path("message").path("content").asText();

        //GPT가 생성한 전체 텍스트 응답 출력
        log.debug("[GPT 응답 - 원본 메시지 content]: \n{}", content);

        // content 중에서 { ... } 형태의 JSON 블록만 추출
        String jsonBlock = extractJsonBlock(content);

        if (jsonBlock == null) {    //JSON 추출 실패 시 정보 출력
            log.error("[GPT 파싱 실패] JSON 블록 추출 실패: user={}, questionId={}, date={}, content={}",
                    seed.getRecordId(), seed.getQuestionId(), seed.getDate(), content);
            throw new IOException("GPT 응답에서 JSON 블록을 추출하지 못했습니다");
        }


        log.debug("[GPT 응답 - JSON 추출]: \n{}", jsonBlock);

        //블록->객체 변환
        JsonNode quizJson = mapper.readTree(jsonBlock);

        //퀴즈 요소 추출 (질문, 정답, 보기)
        String question = quizJson.get("question").asText();
        String answer = quizJson.get("answer").asText();
        List<String> choices = mapper.convertValue(quizJson.get("choices"), List.class);

        //최종 생성된 퀴즈 : 질문/정답/보기
        log.info("[GPT 퀴즈 생성 완료] 질문: '{}', 정답: '{}', 보기: {}", question, answer, choices);

        return QuizResponse.builder()
                .context("기록 기반 회상 퀴즈")
                .question(question)
                .answer(answer)
                .choices(choices)
                .build();
    }

    /**
     * GPT가 응답한 텍스트에서 JSON 블록({ ... })만 추출하는 정규식 메서드
     * - 전체 응답을 JSON으로 간주하면 에러 발생 가능 : gpt가 설명도 json으로 보냄
     * - 정규식으로 가장 먼저 등장하는 JSON 블록을 추출하여 안전하게 처리
     */
    private String extractJsonBlock(String text) {
        var pattern = java.util.regex.Pattern.compile("\\{[\\s\\S]*}");
        var matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }
}

