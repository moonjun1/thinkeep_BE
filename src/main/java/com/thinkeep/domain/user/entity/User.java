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
