package com.thinkeep.domain.Auth.controller;

import com.thinkeep.domain.Auth.dto.LoginRequest;
import com.thinkeep.domain.Auth.dto.LoginResponse;
import com.thinkeep.domain.Auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

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
     * 카카오 로그인
     * POST /api/auth/kakao-login
     */
    @PostMapping("/kakao-login")
    public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        log.info("POST /api/auth/kakao-login - 카카오 로그인 요청: kakaoId={}", request.getKakaoId());

        LoginResponse response = authService.kakaoLogin(request.getKakaoId());

        if (response.isSuccess()) {
            log.info("카카오 로그인 성공: userNo={}", response.getUserNo());
            return ResponseEntity.ok(response);
        } else {
            log.warn("카카오 로그인 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 카카오 로그인 요청 DTO
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KakaoLoginRequest {
        private String kakaoId;
    }
}
