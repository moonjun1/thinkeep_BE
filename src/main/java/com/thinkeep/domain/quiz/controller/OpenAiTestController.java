package com.thinkeep.domain.quiz.controller;

import com.thinkeep.domain.quiz.service.OpenAiTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class OpenAiTestController {

    private final OpenAiTestService openAiTestService;

    @GetMapping("/openai")
    public String testOpenAi() {
        try {
            return openAiTestService.testChatCompletion();  // ChatGPT 응답 반환
        } catch (IOException e) {
            e.printStackTrace();  // 서버 로그에 예외 출력
            return "OpenAI 호출 중 오류 발생: " + e.getMessage();  // 사용자에게 오류 응답
        }
    }

}
