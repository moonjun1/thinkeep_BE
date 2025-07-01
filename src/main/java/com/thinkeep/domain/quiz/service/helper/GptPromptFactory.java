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
                사용자가 시간을 보낸 인물에 대한 회상 퀴즈를 만들어주세요.
                따뜻하고 정감 있는 말투로 질문을 구성해주세요.
                날짜(%s)와 인물 이름(%s)을 반영하여 자연스럽고 중립적인 질문을 만들고,
                정답과 오답 2개를 포함한 3지선다 퀴즈를 생성해주세요.
                질문 문장에는 카테고리명(예: 친구, 가족, 직장 동료 등)을 포함하지 마세요.
                """.formatted(date, answer);

            case "Q3" -> """
                사용자가 작성한 문장에서 음식을 하나 추출하고,
                그 내용을 정답으로 하는 유사한 맥락의 3지선다 회상 퀴즈를 생성해주세요.
                따뜻하고 정감 있는 말투로 질문을 구성해주세요.
                날짜(%s)와 문맥을 자연스럽게 반영해주세요.
                응답: \"%s\"
                """.formatted(date, answer);

            case "Q4" -> """
                사용자가 작성한 문장에서 기억에 남는 활동이나 장면을 하나 요약하여 정답으로 삼고,
                그것을 바탕으로 유사한 맥락의 3지선다 회상 퀴즈를 생성해주세요.
                따뜻하고 정감 있는 말투로 질문을 구성해주세요.
                사용자의 응답 내용에 포함되지 않은 활동이어야 하며,
                보기(choice)는 모두 5~10자 이내의 짧은 명사구 또는 동명사 형태로 작성해 주세요.
                날짜(%s)와 문맥을 자연스럽게 반영해주세요.
                응답: \"%s\"
                """.formatted(date, answer);

            default -> throw new IllegalArgumentException("지원하지 않는 질문 유형: " + seed.getQuestionId());
        };
    }
}
