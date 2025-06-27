package com.thinkeep.domain.Auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    private String nickname;
    private String password;
    private String accessToken;    // JWT 토큰
    private Long expiresIn;        // 만료 시간 (초)
}
