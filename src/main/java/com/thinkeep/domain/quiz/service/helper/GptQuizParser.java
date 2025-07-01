package com.thinkeep.domain.quiz.service.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkeep.domain.quiz.dto.QuestionSeed;
import com.thinkeep.domain.quiz.dto.QuizResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class GptQuizParser {    //GPT가 주는 비정형 응답(텍스트 + JSON)을 파싱 : 정규식 기반 JSON 추출

    private final ObjectMapper mapper = new ObjectMapper();

    public QuizResponse parse(String gptResponseJson, QuestionSeed seed) throws IOException {
        JsonNode root = mapper.readTree(gptResponseJson);
        String content = root.path("choices").get(0).path("message").path("content").asText();

        // JSON 블록 정규식 추출
        String jsonBlock = extractJsonBlock(content);
        if (jsonBlock == null) {
            throw new IOException("GPT 응답에서 JSON 블록을 추출하지 못했습니다: " + content);
        }

        JsonNode quizJson = mapper.readTree(jsonBlock);

        List<String> choices = mapper.convertValue(quizJson.get("choices"), List.class);

        return QuizResponse.builder()
                .context("기록 기반 회상 퀴즈")
                .question(quizJson.get("question").asText())
                .answer(quizJson.get("answer").asText())
                .choices(choices)
                .build();
    }

    /**
     * 텍스트 중 JSON 블록({ ... })을 정규식으로 추출
     */
    private String extractJsonBlock(String text) {
        var pattern = java.util.regex.Pattern.compile("\\{[\\s\\S]*}");
        var matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }
}

