package com.thinkeep.domain.Auth.service;

import com.thinkeep.domain.Auth.dto.LoginRequest;
import com.thinkeep.domain.Auth.dto.LoginResponse;
import com.thinkeep.domain.user.entity.User;
import com.thinkeep.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    /**
     * 일반 로그인 (닉네임 + 비밀번호)
     */
    public LoginResponse login(LoginRequest request) {
        log.info("일반 로그인 시도: nickname={}", request.getNickname());

        // 1. 닉네임으로 사용자 찾기
        Optional<User> userOptional = userRepository.findByNickname(request.getNickname());

        if (userOptional.isEmpty()) {
            log.warn("존재하지 않는 닉네임: {}", request.getNickname());
            return LoginResponse.builder()
                    .success(false)
                    .message("닉네임 또는 비밀번호가 올바르지 않습니다")
                    .build();
        }

        User user = userOptional.get();

        // 2. 카카오 사용자인지 확인
        if (user.getKakaoId() != null) {
            log.warn("카카오 사용자가 일반 로그인 시도: nickname={}", request.getNickname());
            return LoginResponse.builder()
                    .success(false)
                    .message("카카오 로그인을 이용해주세요")
                    .build();
        }

        // 3. 비밀번호 확인
        if (!user.getPassword().equals(request.getPassword())) {
            log.warn("비밀번호 불일치: nickname={}", request.getNickname());
            return LoginResponse.builder()
                    .success(false)
                    .message("닉네임 또는 비밀번호가 올바르지 않습니다")
                    .build();
        }

        // 4. 로그인 성공
        log.info("일반 로그인 성공: userNo={}, nickname={}", user.getUserNo(), user.getNickname());
        return LoginResponse.builder()
                .success(true)
                .message("로그인 성공")
                .userNo(user.getUserNo())
                .nickname(user.getNickname())
                .isKakaoUser(false)
                .build();
    }

    /**
     * 카카오 로그인 (카카오 ID로)
     */
    public LoginResponse kakaoLogin(String kakaoId) {
        log.info("카카오 로그인 시도: kakaoId={}", kakaoId);

        // 1. 카카오 ID로 사용자 찾기
        Optional<User> userOptional = userRepository.findByKakaoId(kakaoId);

        if (userOptional.isEmpty()) {
            log.warn("존재하지 않는 카카오 ID: {}", kakaoId);
            return LoginResponse.builder()
                    .success(false)
                    .message("가입되지 않은 카카오 계정입니다. 먼저 회원가입을 해주세요")
                    .build();
        }

        User user = userOptional.get();

        // 2. 로그인 성공
        log.info("카카오 로그인 성공: userNo={}, nickname={}", user.getUserNo(), user.getNickname());
        return LoginResponse.builder()
                .success(true)
                .message("카카오 로그인 성공")
                .userNo(user.getUserNo())
                .nickname(user.getNickname())
                .isKakaoUser(true)
                .build();
    }
}
