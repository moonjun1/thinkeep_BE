package com.thinkeep.domain.badge.controller;

import com.thinkeep.domain.badge.dto.BadgeRequest;
import com.thinkeep.domain.badge.dto.BadgeResponse;
import com.thinkeep.domain.badge.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
@Tag(name = "뱃지", description = "뱃지 관리 API")
@SecurityRequirement(name = "JWT")
public class BadgeController {

    private final BadgeService badgeService;

    /**
     * 뱃지 전체 조회
     * GET /api/badges
     */
    @Operation(summary = "뱃지 전체 조회", description = "모든 뱃지 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> response = badgeService.getAllBadges();
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 뱃지 조회
     * GET /api/badges/{badgeId}
     */
    @Operation(summary = "특정 뱃지 조회", description = "뱃지 ID로 특정 뱃지를 조회합니다.")
    @GetMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> getBadge(@Parameter(description = "뱃지 ID") @PathVariable Long badgeId) {
        BadgeResponse response = badgeService.getBadge(badgeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 뱃지 등록
     * POST /api/badges
     */
    @Operation(summary = "뱃지 등록", description = "새로운 뱃지를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "뱃지 등록 성공")
    @PostMapping
    public ResponseEntity<BadgeResponse> createBadge(@RequestBody BadgeRequest request) {
        BadgeResponse response = badgeService.createBadge(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 뱃지 수정
     * PUT /api/badges/{badgeId}
     */
    @Operation(summary = "뱃지 수정", description = "기존 뱃지 정보를 수정합니다.")
    @PutMapping("/{badgeId}")
    public ResponseEntity<BadgeResponse> updateBadge(
            @Parameter(description = "뱃지 ID") @PathVariable Long badgeId,
            @RequestBody BadgeRequest request
    ) {
        BadgeResponse response = badgeService.updateBadge(badgeId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 뱃지 삭제
     * DELETE /api/badges/{badgeId}
     */
    @Operation(summary = "뱃지 삭제", description = "뱃지를 삭제합니다.")
    @DeleteMapping("/{badgeId}")
    public ResponseEntity<Void> deleteBadge(@Parameter(description = "뱃지 ID") @PathVariable Long badgeId) {
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.noContent().build();
    }
}