package com.thinkeep.domain.badge.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "condition_json", nullable = false, columnDefinition = "TEXT")
    private String conditionJson;

    public void update(String name, String description, String conditionJson) {
        this.name = name;
        this.description = description;
        this.conditionJson = conditionJson;
    }

}