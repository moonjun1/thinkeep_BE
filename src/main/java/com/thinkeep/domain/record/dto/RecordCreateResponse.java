package com.thinkeep.domain.record.dto;

import com.thinkeep.domain.badge.dto.UserBadgeResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordCreateResponse {
    private RecordResponse record;
    private UserBadgeResponse newBadge;
}
