package com.thinkeep.domain.badge.service;

import com.thinkeep.domain.badge.dto.BadgeRequest;
import com.thinkeep.domain.badge.dto.BadgeResponse;
import com.thinkeep.domain.badge.entity.Badge;
import com.thinkeep.domain.badge.repository.BadgeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 뱃지 관련 서비스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BadgeService {

    private final BadgeRepository badgeRepository;

    /**
     * 모든 뱃지 목록 조회
     */
    public List<BadgeResponse> getAllBadges() {
        log.info("뱃지 전체 목록 조회 시작");
        List<BadgeResponse> responseList = badgeRepository.findAll().stream()
                .map(BadgeResponse::fromEntity)
                .collect(Collectors.toList());
        log.info("뱃지 전체 목록 조회 완료: 총 {}개", responseList.size());
        return responseList;
    }

    /**
     * 특정 뱃지 조회
     */
    public BadgeResponse getBadge(Long badgeId) {
        log.info("뱃지 조회 요청: badgeId={}", badgeId);
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> {
                    log.warn("뱃지 조회 실패: 존재하지 않는 badgeId={}", badgeId);
                    return new IllegalArgumentException("존재하지 않는 뱃지입니다: " + badgeId);
                });
        log.info("뱃지 조회 성공: badgeId={}", badgeId);
        return BadgeResponse.fromEntity(badge);
    }

    /**
     * 뱃지 등록
     */
    @Transactional
    public BadgeResponse createBadge(BadgeRequest request) {
        log.info("뱃지 등록 요청: name={}", request.getName());

        //이미 존재하는 벳지의 경우 등록 실패
        if (badgeRepository.existsByName(request.getName())) {
            log.warn("뱃지 등록 실패: 중복된 이름 {}", request.getName());
            throw new IllegalArgumentException("이미 존재하는 뱃지 이름입니다: " + request.getName());
        }

        Badge badge = badgeRepository.save(request.toEntity());
        log.info("뱃지 등록 완료: badgeId={}", badge.getBadgeId());
        return BadgeResponse.fromEntity(badge);
    }

    /**
     * 뱃지 수정
     */
    @Transactional
    public BadgeResponse updateBadge(Long badgeId, BadgeRequest request) {
        log.info("뱃지 수정 요청: badgeId={}, name={}", badgeId, request.getName());

        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> {
                    log.warn("뱃지 수정 실패: 존재하지 않는 badgeId={}", badgeId);
                    return new IllegalArgumentException("존재하지 않는 뱃지입니다: " + badgeId);
                });

        // 이름이 변경된 경우 중복 여부 체크
        if (!badge.getName().equals(request.getName())
                && badgeRepository.existsByName(request.getName())) {
            log.warn("뱃지 수정 실패: 중복된 이름 {}", request.getName());
            throw new IllegalArgumentException("이미 존재하는 뱃지 이름입니다: " + request.getName());
        }

        badge.update(request.getName(), request.getDescription(), request.getConditionJson());
        log.info("뱃지 수정 완료: badgeId={}", badge.getBadgeId());
        return BadgeResponse.fromEntity(badge);
    }

    /**
     * 뱃지 삭제
     */
    @Transactional
    public void deleteBadge(Long badgeId) {
        log.info("뱃지 삭제 요청: badgeId={}", badgeId);

        if (!badgeRepository.existsById(badgeId)) {
            log.warn("뱃지 삭제 실패: 존재하지 않는 badgeId={}", badgeId);
            throw new IllegalArgumentException("존재하지 않는 뱃지입니다: " + badgeId);
        }

        badgeRepository.deleteById(badgeId);
        log.info("뱃지 삭제 완료: badgeId={}", badgeId);
    }


}

