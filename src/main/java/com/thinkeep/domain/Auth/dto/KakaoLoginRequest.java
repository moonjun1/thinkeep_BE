package com.thinkeep.domain.Auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카카오 로그인 요청 DTO")
public class KakaoLoginRequest {

    @NotBlank(message = "카카오 ID는 필수입니다")
    @Schema(
            description = "카카오에서 제공하는 사용자 고유 ID",
            example = "3234567890",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String kakaoId;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하여야 합니다")
    @Schema(
            description = "카카오 프로필에서 가져온 닉네임 (중복시 자동으로 숫자가 붙습니다)",
            example = "홍길동",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 50
    )
    private String nickname;

    @Schema(
            description = "카카오 프로필 이미지 URL (선택사항, 없으면 null 또는 생략 가능)",
            example = "https://k.kakaocdn.net/dn/profile_image.jpg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String profileImage;

    // 검증 메서드들
    @Schema(hidden = true) // 스웨거에서 숨김
    public boolean isValid() {
        return kakaoId != null && !kakaoId.trim().isEmpty() &&
                nickname != null && !nickname.trim().isEmpty();
    }

    @Schema(hidden = true) // 스웨거에서 숨김
    public boolean hasProfileImage() {
        return profileImage != null && !profileImage.trim().isEmpty();
    }
}