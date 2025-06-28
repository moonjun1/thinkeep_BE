package com.thinkeep.domain.Auth.dto;

import com.thinkeep.domain.user.entity.Gender;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {

    private Long userNo;
    private String nickname;
    private String profileImage;
    private Gender gender;
    private LocalDate birthDate;
    private Integer streakCount;
    private boolean isKakaoUser;
    private LocalDateTime createdAt;
}