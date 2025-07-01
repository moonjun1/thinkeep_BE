package com.thinkeep.domain.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thinkeep.domain.quiz.dto.QuestionSeed;
import com.thinkeep.domain.quiz.dto.QuizResponse;
import com.thinkeep.domain.quiz.service.helper.GptPromptFactory;
import com.thinkeep.domain.quiz.service.helper.GptQuizParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiQuizService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";
    private final OkHttpClient client = new OkHttpClient();
    private final GptPromptFactory promptFactory;
    private final GptQuizParser quizParser;


    public QuizResponse generateQuizFromSeed(QuestionSeed seed) throws IOException {
        String instruction = promptFactory.createPrompt(seed);
        log.info("🔍 GPT 퀴즈 생성 시도 - Seed: {}", seed);

        // JSON 구조 생성
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", MODEL);

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", instruction);
        messages.add(userMessage);

        requestBody.set("messages", messages);

        // 직렬화
        String json = mapper.writeValueAsString(requestBody);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        // 요청 전송
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "empty";
                log.error("GPT 요청 실패: HTTP {}, Body: {}", response.code(), errorBody);
                throw new IOException("GPT 요청 실패: HTTP " + response.code());
            }

            String gptRaw = response.body().string();
            return quizParser.parse(gptRaw, seed);
        }
    }
}
