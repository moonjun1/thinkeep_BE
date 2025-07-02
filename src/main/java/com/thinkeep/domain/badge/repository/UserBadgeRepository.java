package com.thinkeep.domain.badge.repository;

import com.thinkeep.domain.badge.entity.UserBadge;
import com.thinkeep.domain.badge.entity.UserBadgeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadgeId> {

    /**
     * 복합키 기준 - 중복 뱃지 부여 여부 확인
     * @param id UserBadgeId (userNo + badgeId)
     * @return 존재 여부
     */
    boolean existsById(UserBadgeId id);
}
