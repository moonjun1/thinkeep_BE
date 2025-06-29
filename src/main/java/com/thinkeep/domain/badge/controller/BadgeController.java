package com.thinkeep.domain.badge.controller;

import com.thinkeep.domain.badge.dto.BadgeRequest;
import com.thinkeep.domain.badge.dto.BadgeResponse;
import com.thinkeep.domain.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    /**
     * 뱃지 전체 조회
     * GET /api/badges
     */
    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> response = badgeService.getAllBadges();
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 뱃지 조회
     * GET /api/badges/{badgeId}
     */
    @GetMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> getBadge(@PathVariable Long badgeId) {
        BadgeResponse response = badgeService.getBadge(badgeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 뱃지 등록
     * POST /api/badges
     */
    @PostMapping
    public ResponseEntity<BadgeResponse> createBadge(@RequestBody BadgeRequest request) {
        BadgeResponse response = badgeService.createBadge(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 뱃지 수정
     * PUT /api/badges/{badgeId}
     */
    @PutMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> updateBadge(
            @PathVariable Long badgeId,
            @RequestBody BadgeRequest request
    ) {
        BadgeResponse response = badgeService.updateBadge(badgeId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 뱃지 삭제
     * DELETE /api/badges/{badgeId}
     */
    @DeleteMapping("/{badgeId}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long badgeId) {
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.noContent().build();
    }

}

