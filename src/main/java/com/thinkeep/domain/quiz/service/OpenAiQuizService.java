package com.thinkeep.domain.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkeep.domain.quiz.dto.QuestionSeed;
import com.thinkeep.domain.quiz.dto.QuizResponse;
import com.thinkeep.domain.quiz.service.helper.GptPromptFactory;
import com.thinkeep.domain.quiz.service.helper.GptQuizParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiQuizService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";
    private final OkHttpClient client = new OkHttpClient();
    private final GptPromptFactory promptFactory;
    private final GptQuizParser quizParser;

    private final String apiKey = System.getenv("OPENAI_API_KEY");

    public QuizResponse generateQuizFromSeed(QuestionSeed seed) throws IOException {
        String instruction = promptFactory.createPrompt(seed);

        String jsonRequest = """
        {
          "model": "%s",
          "messages": [{"role": "user", "content": "%s"}]
        }
        """.formatted(MODEL, instruction);

        RequestBody body = RequestBody.create(jsonRequest, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GPT 요청 실패: " + response);
            }
            String gptRaw = response.body().string();
            return quizParser.parse(gptRaw, seed);
        }
    }
}
