package com.thinkeep.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Long userNo;

    // === 로그인 관련 ===
    @Column(name = "nickname", unique = true, nullable = false, length = 50)
    private String nickname;        // 로그인 ID (unique)

    @Column(name = "password", length = 255)
    private String password;        // 일반 로그인용 (카카오는 null)

    @Column(name = "kakao_id", unique = true, length = 100)
    private String kakaoId;         // 카카오 회원번호 (일반은 null)

    // === 개인정보 ===
    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;          // 성별 (선택사항)

    @Column(name = "birth_date")
    private LocalDate birthDate;    // 생년월일 (선택사항)

    // === 앱 관련 ===
    @Column(name = "streak_count", nullable = false)
    @Builder.Default
    private Integer streakCount = 0;

    @Column(name = "last_record_date")
    private LocalDate lastRecordDate;  // 마지막 출석 날짜

    @Column(name = "badge_3_days_achieved")
    @Builder.Default
    private Boolean badge3DaysAchieved = false; // 시작의 뱃지 (3일)

    @Column(name = "badge_7_days_achieved")
    @Builder.Default
    private Boolean badge7DaysAchieved = false; // 작은 습관의 뱃지 (7일)

    @Column(name = "badge_14_days_achieved")
    @Builder.Default
    private Boolean badge14DaysAchieved = false;    //흔들림 없는 뱃지(14일)

    @Column(name = "badge_30_days_achieved")
    @Builder.Default
    private Boolean badge30DaysAchieved = false;    //나를 위한 루틴 뱃지 (30일)


    // === 공통 필드 ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === 생성/수정 시간 자동 설정 ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
