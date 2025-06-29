package com.thinkeep.domain.record.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private boolean success;
    private String message;
    private java.time.LocalDateTime timestamp;
}