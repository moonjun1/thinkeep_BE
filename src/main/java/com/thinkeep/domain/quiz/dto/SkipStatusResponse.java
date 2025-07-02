package com.thinkeep.domain.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkipStatusResponse {
    private int skippedCount;   // 오늘 건너뛴 횟수
    private int remainingSkips; // 남은 건너뛰기 가능 횟수
}
