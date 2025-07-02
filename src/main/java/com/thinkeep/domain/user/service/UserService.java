package com.thinkeep.domain.user.service;


import com.thinkeep.domain.badge.dto.UserBadgeRequest;
import com.thinkeep.domain.badge.service.UserBadgeService;
import com.thinkeep.domain.user.dto.CreateRequest;
import com.thinkeep.domain.user.dto.Response;
import com.thinkeep.domain.user.dto.UpdateRequest;
import com.thinkeep.domain.user.entity.User;
import com.thinkeep.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserBadgeService  userBadgeService;

    /**
     * 사용자 생성
     */
    @Transactional
    public Response createUser(CreateRequest request) {
        log.info("사용자 생성 시작: nickname={}", request.getNickname());

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + request.getNickname());
        }

        // 카카오 ID 중복 확인 (카카오 회원가입인 경우)
        if (request.isKakaoSignup() && userRepository.existsByKakaoId(request.getKakaoId())) {
            throw new IllegalArgumentException("이미 가입된 카카오 계정입니다");
        }

        // 회원가입 유형 검증
        if (!request.isGeneralSignup() && !request.isKakaoSignup()) {
            throw new IllegalArgumentException("일반 회원가입 또는 카카오 회원가입 정보가 필요합니다");
        }

        // User 엔티티 생성
        User user = User.builder()
                .nickname(request.getNickname())
                .password(request.getPassword())
                .kakaoId(request.getKakaoId())
                .profileImage(request.getProfileImage())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .streakCount(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 생성 완료: userNo={}, nickname={}", savedUser.getUserNo(), savedUser.getNickname());

        return convertToResponse(savedUser);
    }

    /**
     * 사용자 조회 (ID)
     */
    public Response getUserById(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userNo));

        return convertToResponse(user);
    }

    /**
     * 사용자 조회 (닉네임)
     */
    public Response getUserByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + nickname));

        return convertToResponse(user);
    }

    /**
     * 모든 사용자 조회
     */
    public List<Response> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public Response updateUser(Long userNo, UpdateRequest request) {
        log.info("사용자 수정 시작: userNo={}", userNo);

        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userNo));

        // 수정 가능한 필드만 업데이트
        if (StringUtils.hasText(request.getProfileImage())) {
            user.setProfileImage(request.getProfileImage());
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }

        // 일반 사용자만 비밀번호 변경 가능
        if (StringUtils.hasText(request.getPassword()) && user.getKakaoId() == null) {
            user.setPassword(request.getPassword());
        }

        User updatedUser = userRepository.save(user);
        log.info("사용자 수정 완료: userNo={}", updatedUser.getUserNo());

        return convertToResponse(updatedUser);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long userNo) {
        log.info("사용자 삭제 시작: userNo={}", userNo);

        if (!userRepository.existsById(userNo)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userNo);
        }

        userRepository.deleteById(userNo);
        log.info("사용자 삭제 완료: userNo={}", userNo);
    }

    /**
     * 스트릭 카운트 증가
     */

    @Transactional
    public Response increaseStreakCount(Long userNo) {
        log.info("스트릭 카운트 증가: userNo={}", userNo);

        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userNo));

        user.setStreakCount(user.getStreakCount() + 1);
        User updatedUser = userRepository.save(user);

        log.info("스트릭 카운트 증가 완료: userNo={}, streakCount={}",
                updatedUser.getUserNo(), updatedUser.getStreakCount());

        return convertToResponse(updatedUser);
    }

    /**
     * 출석 체크 및 뱃지 부여
     */
    @Transactional
    public void checkStreakAndAssignBadge(Long userNo, LocalDate today) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userNo));

        // 24시간 이상 공백이면 스트릭 초기화
        if (user.getLastRecordDate() == null || ChronoUnit.DAYS.between(user.getLastRecordDate(), today) >= 2) {
            user.setStreakCount(1);
        } else if (!user.getLastRecordDate().isEqual(today)) {
            user.setStreakCount(user.getStreakCount() + 1);
        }

        user.setLastRecordDate(today);

        int streak = user.getStreakCount();

        // === 조건에 따라 뱃지 지급 ===
        if (streak == 3 && !Boolean.TRUE.equals(user.getBadge3DaysAchieved())) {
            assignBadge(user, 1L); // 뱃지 ID 1
            user.setBadge3DaysAchieved(true);
        }

        if (streak == 7 && !Boolean.TRUE.equals(user.getBadge7DaysAchieved())) {
            assignBadge(user, 2L);
            user.setBadge7DaysAchieved(true);
        }

        if (streak == 14 && !Boolean.TRUE.equals(user.getBadge14DaysAchieved())) {
            assignBadge(user, 3L);
            user.setBadge14DaysAchieved(true);
        }

        if (streak == 30 && !Boolean.TRUE.equals(user.getBadge30DaysAchieved())) {
            assignBadge(user, 4L);
            user.setBadge30DaysAchieved(true);
        }

        userRepository.save(user);
    }
    private void assignBadge(User user, Long badgeId) {
        try {
            userBadgeService.assignBadgeToUser(
                    new UserBadgeRequest(user.getUserNo(), badgeId)
            );
            log.info("뱃지 지급 완료: userNo={}, badgeId={}", user.getUserNo(), badgeId);
        } catch (IllegalStateException e) {
            log.warn("이미 지급된 뱃지: {}", e.getMessage());
        }
    }


    // === 변환 메서드 ===
    private Response convertToResponse(User user) {
        return Response.builder()
                .userNo(user.getUserNo())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .streakCount(user.getStreakCount())
                .isKakaoUser(user.getKakaoId() != null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}