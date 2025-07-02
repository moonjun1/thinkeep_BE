package com.thinkeep.domain.badge.dto;

import com.thinkeep.domain.badge.entity.Badge;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadgeRequest {
    private String name;
    private String description;
    private String conditionJson;

    public Badge toEntity() {
        return Badge.builder()
                .name(name)
                .description(description)
                .conditionJson(conditionJson)
                .build();
    }
}
