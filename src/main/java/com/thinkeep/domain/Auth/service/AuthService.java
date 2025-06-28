package com.thinkeep.domain.Auth.service;

import com.thinkeep.domain.Auth.dto.LoginRequest;
import com.thinkeep.domain.Auth.dto.LoginResponse;
import com.thinkeep.domain.Auth.dto.UserInfo;
import com.thinkeep.domain.user.entity.User;
import com.thinkeep.domain.user.repository.UserRepository;
import com.thinkeep.global.jwt.JwtUtil;
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
    private final JwtUtil jwtUtil;

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

        // 4. JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(user);

        // 5. 로그인 성공
        log.info("일반 로그인 성공: userNo={}, nickname={}", user.getUserNo(), user.getNickname());
        return LoginResponse.builder()
                .success(true)
                .message("로그인 성공")
                .userNo(user.getUserNo())
                .nickname(user.getNickname())
                .isKakaoUser(false)
                .accessToken(accessToken)
                .expiresIn(3600L)
                .build();
    }

    /**
     * 카카오 로그인 (자동 회원가입 포함)
     */
    @Transactional
    public LoginResponse kakaoLogin(String kakaoId, String nickname, String profileImage) {
        log.info("카카오 로그인 시도: kakaoId={}, nickname={}", kakaoId, nickname);

        // 1. 카카오 ID로 기존 사용자 찾기
        Optional<User> userOptional = userRepository.findByKakaoId(kakaoId);

        if (userOptional.isPresent()) {
            // === 기존 사용자 로그인 ===
            User user = userOptional.get();

            // 프로필 이미지 업데이트 (카카오에서 변경될 수 있음)
            if (profileImage != null && !profileImage.equals(user.getProfileImage())) {
                user.setProfileImage(profileImage);
                userRepository.save(user);
                log.info("프로필 이미지 업데이트: userNo={}", user.getUserNo());
            }

            String accessToken = jwtUtil.generateToken(user);

            log.info("기존 카카오 사용자 로그인 성공: userNo={}, nickname={}",
                    user.getUserNo(), user.getNickname());
            return LoginResponse.builder()
                    .success(true)
                    .message("카카오 로그인 성공")
                    .userNo(user.getUserNo())
                    .nickname(user.getNickname())
                    .isKakaoUser(true)
                    .accessToken(accessToken)
                    .expiresIn(3600L)
                    .build();
        }

        // === 신규 사용자 자동 회원가입 ===
        log.info("신규 카카오 사용자 - 자동 회원가입 진행: kakaoId={}", kakaoId);

        // 2. 닉네임 중복 처리
        String finalNickname = generateUniqueNickname(nickname);

        // 3. 새 사용자 생성
        User newUser = User.builder()
                .kakaoId(kakaoId)
                .nickname(finalNickname)
                .profileImage(profileImage)
                .password(null)  // 카카오 사용자는 비밀번호 없음
                .streakCount(0)
                .build();

        User savedUser = userRepository.save(newUser);
        String accessToken = jwtUtil.generateToken(savedUser);

        log.info("카카오 자동 회원가입 및 로그인 성공: userNo={}, nickname={}",
                savedUser.getUserNo(), savedUser.getNickname());

        return LoginResponse.builder()
                .success(true)
                .message("카카오 회원가입 및 로그인 성공")
                .userNo(savedUser.getUserNo())
                .nickname(savedUser.getNickname())
                .isKakaoUser(true)
                .accessToken(accessToken)
                .expiresIn(3600L)
                .build();
    }

    /**
     * 닉네임 중복 시 유니크한 닉네임 생성
     */
    private String generateUniqueNickname(String baseNickname) {
        String nickname = baseNickname;
        int suffix = 1;

        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + suffix;
            suffix++;
        }

        log.info("유니크 닉네임 생성: {} -> {}", baseNickname, nickname);
        return nickname;
    }

    /**
     * 로그아웃 처리 - 🟢 새로 추가된 부분
     */
    public LoginResponse logout() {
        log.info("로그아웃 처리");

        return LoginResponse.builder()
                .success(true)
                .message("로그아웃 성공")
                .build();
    }

    /**
     * 현재 사용자 정보 조회 - 🟢 새로 추가된 부분
     */
    public UserInfo getCurrentUser(Long userNo) {
        log.info("현재 사용자 정보 조회: userNo={}", userNo);

        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return UserInfo.builder()
                .userNo(user.getUserNo())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .streakCount(user.getStreakCount())
                .isKakaoUser(user.getKakaoId() != null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}