package com.thinkeep.domain.quiz.service.helper;

import com.thinkeep.domain.quiz.dto.QuestionSeed;
import org.springframework.stereotype.Component;

@Component
public class GptPromptFactory {

    public String createPrompt(QuestionSeed seed) {
        String date = seed.getDate().toString();
        String answer = seed.getAnswer().trim();

        return switch (seed.getQuestionId()) {
            case "Q2" -> """
                아래 조건을 만족하는 JSON 형식의 회상 퀴즈를 생성해주세요.

                [조건]
                1. 사용자가 %s에 보낸 인물 이름은 "%s"입니다.
                2. 질문은 따뜻하고 정감 있는 말투로 구성해주세요.
                3. 질문 내용에는 인물의 '이름'은 자연스럽게 등장하지만, 인물의 '카테고리'는 포함하지 마세요.
                4. 보기는 총 3개이며, 하나는 정답(위 인물), 나머지 두 개는 plausibly 유사한 상황에서 등장할 수 있는 인물이어야 합니다.

                [출력 형식]
                ```json
                {
                  "question": "문장형 질문 내용",
                  "choices": ["보기1", "보기2", "보기3"],
                  "answer": "정답"
                }
                ```

                위와 같은 JSON만 출력해주세요. 설명 없이 JSON 블록만 응답해야 합니다.
                """.formatted(date, answer);

            case "Q3" -> """
                아래 조건을 만족하는 JSON 형식의 회상 퀴즈를 생성해주세요.

                [조건]
                1. 사용자가 %s에 먹은 음식은 "%s"입니다.
                2. 응답 문장을 참고하여 자연스러운 질문을 구성해주세요.
                3. 보기(choice)는 음식 이름으로 구성되며, 총 3개입니다. 하나는 정답(위 음식), 나머지 두 개는 유사한 한국 음식이어야 합니다.

                [출력 형식]
                ```json
                {
                  "question": "문장형 질문 내용",
                  "choices": ["보기1", "보기2", "보기3"],
                  "answer": "정답"
                }
                ```

                위와 같은 JSON만 출력해주세요. 설명 없이 JSON 블록만 응답해야 합니다.
                """.formatted(date, answer);

            case "Q4" -> """
                아래 조건을 만족하는 JSON 형식의 회상 퀴즈를 생성해주세요.

                [조건]
                1. 사용자의 활동 응답은 다음과 같습니다: "%s" (날짜: %s)
                2. 이 응답에서 기억에 남는 활동이나 장면을 요약하여 정답으로 삼고, 그와 관련된 3지선다 퀴즈를 구성해주세요.
                3. 보기는 모두 5~10자 이내의 짧은 명사구 또는 동명사 형태여야 합니다.
                4. 질문은 따뜻하고 정감 있는 말투로 구성해주세요.

                [출력 형식]
                ```json
                {
                  "question": "문장형 질문 내용",
                  "choices": ["보기1", "보기2", "보기3"],
                  "answer": "정답"
                }
                ```

                위와 같은 JSON만 출력해주세요. 설명 없이 JSON 블록만 응답해야 합니다.
                """.formatted(answer, date);

            default -> throw new IllegalArgumentException("지원하지 않는 질문 유형: " + seed.getQuestionId());
        };
    }
}
