package com.thinkeep.domain.user.dto;

import com.thinkeep.domain.user.entity.Gender;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {

    private Long userNo;
    private String nickname;
    private String profileImage;
    private Gender gender;
    private LocalDate birthDate;
    private Integer streakCount;
    private boolean isKakaoUser;    // 카카오 사용자 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
