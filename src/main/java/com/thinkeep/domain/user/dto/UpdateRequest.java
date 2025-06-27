package com.thinkeep.domain.user.dto;

import com.thinkeep.domain.user.entity.Gender;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRequest {

    private String profileImage;
    private Gender gender;
    private LocalDate birthDate;
    private String password;        // 비밀번호 변경 시에만 입력
}
