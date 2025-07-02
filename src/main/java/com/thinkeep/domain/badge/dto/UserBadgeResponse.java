package com.thinkeep.domain.badge.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserBadgeResponse {
    private Long userNo;
    private Long badgeId;
    private LocalDateTime awardedAt;
}
