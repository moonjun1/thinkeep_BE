package com.thinkeep.domain.Auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private boolean success;        // 로그인 성공 여부
    private String message;         // 응답 메시지
    private Long userNo;           // 사용자 번호 (성공시에만)
    private String nickname;       // 닉네임 (성공시에만)
    private boolean isKakaoUser;   // 카카오 사용자 여부 (성공시에만)
    private String accessToken;    // JWT 토큰
    private Long expiresIn;        // 만료 시간 (초)
}
