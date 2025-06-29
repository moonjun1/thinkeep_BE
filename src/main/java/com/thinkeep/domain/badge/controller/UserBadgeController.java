package com.thinkeep.domain.badge.controller;

import com.thinkeep.domain.badge.dto.UserBadgeRequest;
import com.thinkeep.domain.badge.dto.UserBadgeResponse;
import com.thinkeep.domain.badge.service.UserBadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-badges")
@RequiredArgsConstructor
public class UserBadgeController {

    private final UserBadgeService userBadgeService;

    /**
     * 사용자에게 뱃지 부여
     * POST /api/user-badges
     */
    @PostMapping
    public ResponseEntity<UserBadgeResponse> assignBadgeToUser(@RequestBody UserBadgeRequest request) {
        UserBadgeResponse response = userBadgeService.assignBadgeToUser(request);
        return ResponseEntity.ok(response);
    }

}
