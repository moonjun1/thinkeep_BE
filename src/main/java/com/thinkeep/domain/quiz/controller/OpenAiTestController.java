package com.thinkeep.domain.quiz.controller;

import com.thinkeep.domain.quiz.service.OpenAiTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "OpenAI 테스트", description = "OpenAI API 연결 테스트용 API")
public class OpenAiTestController {

    private final OpenAiTestService openAiTestService;

    /**
     * OpenAI API 연결 테스트
     * GET /api/test/openai
     */
    @Operation(
            summary = "OpenAI API 연결 테스트",
            description = "ChatGPT API와의 연결 상태를 확인합니다. 개발 및 디버깅용입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OpenAI API 연결 성공 및 응답 반환"),
            @ApiResponse(responseCode = "500", description = "OpenAI API 호출 실패 또는 네트워크 오류")
    })
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