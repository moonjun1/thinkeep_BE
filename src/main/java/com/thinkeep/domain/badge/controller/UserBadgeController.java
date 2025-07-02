package com.thinkeep.domain.badge.controller;

import com.thinkeep.domain.badge.dto.UserBadgeRequest;
import com.thinkeep.domain.badge.dto.UserBadgeResponse;
import com.thinkeep.domain.badge.service.UserBadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-badges")
@RequiredArgsConstructor
@Tag(name = "사용자 뱃지", description = "사용자에게 뱃지 부여 API")
@SecurityRequirement(name = "JWT")
public class UserBadgeController {

    private final UserBadgeService userBadgeService;

    /**
     * 사용자에게 뱃지 부여
     * POST /api/user-badges
     */
    @Operation(summary = "사용자에게 뱃지 부여", description = "특정 사용자에게 뱃지를 부여합니다.")
    @ApiResponse(responseCode = "200", description = "뱃지 부여 성공")
    @PostMapping
    public ResponseEntity<UserBadgeResponse> assignBadgeToUser(@RequestBody UserBadgeRequest request) {
        UserBadgeResponse response = userBadgeService.assignBadgeToUser(request);
        return ResponseEntity.ok(response);
    }
}