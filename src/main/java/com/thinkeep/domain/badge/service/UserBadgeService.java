package com.thinkeep.domain.badge.service;

import com.thinkeep.domain.badge.dto.UserBadgeRequest;
import com.thinkeep.domain.badge.dto.UserBadgeResponse;
import com.thinkeep.domain.badge.entity.Badge;
import com.thinkeep.domain.badge.entity.UserBadge;
import com.thinkeep.domain.badge.entity.UserBadgeId;
import com.thinkeep.domain.badge.repository.BadgeRepository;
import com.thinkeep.domain.badge.repository.UserBadgeRepository;
import com.thinkeep.domain.user.entity.User;
import com.thinkeep.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBadgeService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * 사용자에게 뱃지 부여
     * 이미 부여된 경우 예외 발생
     */
    public UserBadgeResponse assignBadgeToUser(UserBadgeRequest request) {
        log.info("뱃지 부여 요청: userNo={}, badgeId={}", request.getUserNo(), request.getBadgeId());

        // 유저 조회
        User user = userRepository.findById(request.getUserNo())
                .orElseThrow(() -> {
                    log.error("뱃지 부여 실패: 존재하지 않는 유저 userNo={}", request.getUserNo());
                    return new IllegalArgumentException("유저 없음");
                });

        // 뱃지 조회
        Badge badge = badgeRepository.findById(request.getBadgeId())
                .orElseThrow(() -> {
                    log.error("뱃지 부여 실패: 존재하지 않는 뱃지 badgeId={}", request.getBadgeId());
                    return new IllegalArgumentException("뱃지 없음");
                });

        // 복합 키 생성
        UserBadgeId id = new UserBadgeId(user.getUserNo(), badge.getBadgeId());

        // 중복 체크
        if (userBadgeRepository.existsById(id)) {
            log.warn("중복 뱃지 부여 시도: userNo={}, badgeId={}", user.getUserNo(), badge.getBadgeId());
            throw new IllegalStateException("이미 부여된 뱃지입니다.");
        }

        // 뱃지 부여 처리
        UserBadge userBadge = UserBadge.builder()
                .id(id)
                .user(user)
                .badge(badge)
                .awardedAt(LocalDateTime.now())
                .build();

        userBadgeRepository.save(userBadge);
        log.info("뱃지 부여 성공: userNo={}, badgeId={}, awardedAt={}",
                user.getUserNo(), badge.getBadgeId(), userBadge.getAwardedAt());

        return UserBadgeResponse.builder()
                .userNo(user.getUserNo())
                .badgeId(badge.getBadgeId())
                .awardedAt(userBadge.getAwardedAt())
                .build();
    }


}

