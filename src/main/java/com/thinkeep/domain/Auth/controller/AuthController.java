package com.thinkeep.domain.Auth.controller;

import com.thinkeep.domain.Auth.dto.KakaoLoginRequest;
import com.thinkeep.domain.Auth.dto.LoginRequest;
import com.thinkeep.domain.Auth.dto.LoginResponse;
import com.thinkeep.domain.Auth.dto.UserInfo;
import com.thinkeep.domain.Auth.service.AuthService;
import com.thinkeep.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * 일반 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - 일반 로그인 요청: nickname={}", request.getNickname());

        LoginResponse response = authService.login(request);

        if (response.isSuccess()) {
            log.info("일반 로그인 성공: userNo={}", response.getUserNo());
            return ResponseEntity.ok(response);
        } else {
            log.warn("일반 로그인 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout() {
        log.info("POST /api/auth/logout - 로그아웃 요청");

        LoginResponse response = authService.logout();
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 사용자 정보 조회
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(Authentication authentication) {
        log.info("GET /api/auth/me - 현재 사용자 정보 조회");

        try {
            // Spring Security에서 현재 로그인된 사용자 번호 가져오기
            Long userNo = (Long) authentication.getPrincipal();

            UserInfo userInfo = authService.getCurrentUser(userNo);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 카카오 로그인
     * POST /api/auth/kakao-login
     */
    @PostMapping("/kakao-login")
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        log.info("POST /api/auth/kakao-login - 카카오 로그인 요청: kakaoId={}, nickname={}",
                request.getKakaoId(), request.getNickname());

        // AuthService에 위임 (컨트롤러에서 직접 처리하지 말고)
        LoginResponse response = authService.kakaoLogin(
                request.getKakaoId(),
                request.getNickname(),
                request.getProfileImage()
        );

        if (response.isSuccess()) {
            log.info("카카오 로그인 성공: userNo={}", response.getUserNo());
            return ResponseEntity.ok(response);
        } else {
            log.warn("카카오 로그인 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}