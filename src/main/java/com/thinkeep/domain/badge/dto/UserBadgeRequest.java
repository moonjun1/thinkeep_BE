package com.thinkeep.domain.badge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeRequest {
    private Long userNo;
    private Long badgeId;
}
