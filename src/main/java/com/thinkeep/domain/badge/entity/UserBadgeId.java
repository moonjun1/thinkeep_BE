package com.thinkeep.domain.badge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name = "users")
public class UserBadgeId implements Serializable {
    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "badge_id")
    private Long badgeId;
}
