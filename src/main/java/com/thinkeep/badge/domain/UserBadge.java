package com.thinkeep.badge.domain;


import com.thinkeep.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users_badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {

    @EmbeddedId
    private UserBadgeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userNo")
    @JoinColumn(name = "user_no", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("badgeId")
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "awarded_at")
    private LocalDateTime awardedAt;
}
