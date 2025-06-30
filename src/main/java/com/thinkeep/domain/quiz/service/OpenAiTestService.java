package com.thinkeep.domain.quiz.service;

import com.thinkeep.global.config.OpenAiConfig;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OpenAiTestService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();

    public String testChatCompletion() throws IOException {
        String apiKey = OpenAiConfig.getOpenAiKey();  // .env에서 불러온 키
        String json = """
                {
                  "model": "gpt-4o",
                  "messages": [{"role": "user", "content": "안녕 GPT, 잘 작동하니?"}]
                }
                """;

        RequestBody body = RequestBody.create(
                json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }
}
