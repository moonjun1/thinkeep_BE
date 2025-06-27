package com.thinkeep.domain.user.controller;

import com.thinkeep.domain.user.dto.CreateRequest;
import com.thinkeep.domain.user.dto.Response;
import com.thinkeep.domain.user.dto.UpdateRequest;
import com.thinkeep.domain.user.service.UserService;
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
public class UserController {

    private final UserService userService;

    /**
     * 사용자 생성 (회원가입)
     * POST /api/users
     */
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
    @GetMapping("/{userNo}")
    public ResponseEntity<Response> getUserById(@PathVariable Long userNo) {
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
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<Response> getUserByNickname(@PathVariable String nickname) {
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
    @PutMapping("/{userNo}")
    public ResponseEntity<Response> updateUser(@PathVariable Long userNo, @RequestBody UpdateRequest request) {
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
    @DeleteMapping("/{userNo}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userNo) {
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
    @PostMapping("/{userNo}/streak")
    public ResponseEntity<Response> increaseStreakCount(@PathVariable Long userNo) {
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
