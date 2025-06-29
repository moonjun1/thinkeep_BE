package com.thinkeep.domain.record.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

// === 1. 일기 작성 요청 DTO ===
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordCreateRequest {

    /**
     * 각 질문별 답변들
     * 예시: {"Q1": "HAPPY", "Q2": "딸과 함께", "Q3": "김치찌개", "Q4": "공원 산책"}
     */
    private Map<String, String> answers;

    /**
     * 검증 메서드
     */
    public boolean isValid() {
        return answers != null &&
                !answers.isEmpty() &&
                answers.containsKey("Q1") &&
                answers.containsKey("Q2") &&
                answers.containsKey("Q3") &&
                answers.containsKey("Q4");
    }

    /**
     * 필수 답변들이 모두 비어있지 않은지 확인
     */
    public boolean hasAllRequiredAnswers() {
        if (!isValid()) return false;

        return isNotEmpty(answers.get("Q1")) &&
                isNotEmpty(answers.get("Q2")) &&
                isNotEmpty(answers.get("Q3")) &&
                isNotEmpty(answers.get("Q4"));
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}