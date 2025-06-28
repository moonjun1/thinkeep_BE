package com.thinkeep.domain.Auth.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public  class KakaoLoginRequest {

    private String kakaoId;
    private String nickname;        // 카카오 닉네임 (필수)
    private String profileImage;    // 프로필 이미지 URL (선택사항)

    // 검증 메서드들
    public boolean isValid() {
        return kakaoId != null && !kakaoId.trim().isEmpty() &&
                nickname != null && !nickname.trim().isEmpty();
    }
}