package com.thinkeep.domain.user.dto;

import com.thinkeep.domain.user.entity.Gender;
import lombok.*;
import org.hibernate.annotations.processing.Pattern;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRequest {

    private String nickname;
    private String password;        // 일반 회원가입 시 필수, 카카오는 null
    private String kakaoId;         // 카카오 회원가입 시 필수, 일반은 null
    private String profileImage;
    private Gender gender;
    private LocalDate birthDate;

    // 일반 회원가입인지 확인
    public boolean isGeneralSignup() {
        return password != null && !password.trim().isEmpty();
    }

    // 카카오 회원가입인지 확인
    public boolean isKakaoSignup() {
        return kakaoId != null && !kakaoId.trim().isEmpty();
    }
}