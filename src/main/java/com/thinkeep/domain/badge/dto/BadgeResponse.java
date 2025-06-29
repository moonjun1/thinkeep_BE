package com.thinkeep.domain.badge.dto;

import com.thinkeep.domain.badge.entity.Badge;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BadgeResponse {
    private Long badgeId;
    private String name;
    private String description;
    private String conditionJson;

    public static BadgeResponse fromEntity(Badge badge) {
        return new BadgeResponse(
                badge.getBadgeId(),
                badge.getName(),
                badge.getDescription(),
                badge.getConditionJson()
        );
    }
}
