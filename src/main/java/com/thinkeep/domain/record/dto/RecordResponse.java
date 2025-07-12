package com.thinkeep.domain.record.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordResponse {

    private Long recordId;
    private Long userNo;
    private LocalDate date;
    private Map<String, String> answers;

    // 🆕 감정 필드 추가
    private String emotion;

    // 메타 정보
    private boolean isComplete;
    private boolean isToday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 간단한 통계
    private Integer answerCount;     // 답변한 질문 수 (감정 포함)
    private String statusMessage;   // 상태 메시지
}