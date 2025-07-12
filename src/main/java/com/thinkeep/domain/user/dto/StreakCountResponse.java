package com.thinkeep.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StreakCountResponse {  //스트릭 카운트값 반환 DTO
    private Long userNo;
    private int streakCount;
}
