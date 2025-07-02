package com.thinkeep.domain.user.controller;

import com.thinkeep.domain.user.dto.CreateRequest;
import com.thinkeep.domain.user.dto.Response;
import com.thinkeep.domain.user.dto.UpdateRequest;
import com.thinkeep.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "사용자", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    /**
     * 사용자 생성 (회원가입)
     * POST /api/users
     */
    @Operation(summary = "회원가입", description = "새로운 사용자를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @PostMapping
    public ResponseEntity<Response> createUser(@RequestBody CreateRequest request) {
        log.info("POST /api/users - 사용자 생성 요청: nickname={}", request.getNickname());

        try {
            Response response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("사용자 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용자 조회 (ID)
     * GET /api/users/{userNo}
     */
    @Operation(summary = "사용자 조회", description = "사용자 번호로 사용자 정보를 조회합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{userNo}")
    public ResponseEntity<Response> getUserById(@Parameter(description = "사용자 번호") @PathVariable Long userNo) {
        log.info("GET /api/users/{} - 사용자 조회", userNo);

        try {
            Response response = userService.getUserById(userNo);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("사용자 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자 조회 (닉네임)
     * GET /api/users/nickname/{nickname}
     */
    @Operation(summary = "닉네임으로 사용자 조회", description = "닉네임으로 사용자 정보를 조회합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<Response> getUserByNickname(@Parameter(description = "사용자 닉네임") @PathVariable String nickname) {
        log.info("GET /api/users/nickname/{} - 닉네임으로 사용자 조회", nickname);

        try {
            Response response = userService.getUserByNickname(nickname);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("사용자 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 모든 사용자 조회
     * GET /api/users
     */
    @Operation(summary = "모든 사용자 조회", description = "모든 사용자 목록을 조회합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ResponseEntity<List<Response>> getAllUsers() {
        log.info("GET /api/users - 사용자 목록 조회");

        List<Response> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 정보 수정
     * PUT /api/users/{userNo}
     */
    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{userNo}")
    public ResponseEntity<Response> updateUser(@Parameter(description = "사용자 번호") @PathVariable Long userNo, @RequestBody UpdateRequest request) {
        log.info("PUT /api/users/{} - 사용자 정보 수정", userNo);

        try {
            Response response = userService.updateUser(userNo, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("사용자 수정 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자 삭제
     * DELETE /api/users/{userNo}
     */
    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{userNo}")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "사용자 번호") @PathVariable Long userNo) {
        log.info("DELETE /api/users/{} - 사용자 삭제", userNo);

        try {
            userService.deleteUser(userNo);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("사용자 삭제 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 스트릭 카운트 증가
     * POST /api/users/{userNo}/streak
     */
    @Operation(summary = "스트릭 카운트 증가", description = "사용자의 연속 일기 작성 카운트를 증가시킵니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{userNo}/streak")
    public ResponseEntity<Response> increaseStreakCount(@Parameter(description = "사용자 번호") @PathVariable Long userNo) {
        log.info("POST /api/users/{}/streak - 스트릭 카운트 증가", userNo);

        try {
            Response response = userService.increaseStreakCount(userNo);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("스트릭 카운트 증가 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}