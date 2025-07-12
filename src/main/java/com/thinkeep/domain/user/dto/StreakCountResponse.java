package com.thinkeep.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StreakCountResponse {
    private Long userNo;
    private int streakCount;
}
