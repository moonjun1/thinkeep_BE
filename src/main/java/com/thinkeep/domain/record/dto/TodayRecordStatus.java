package com.thinkeep.domain.record.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayRecordStatus {

    private boolean hasRecord;           // 오늘 기록 존재 여부
    private LocalDate date;              // 날짜
    private RecordResponse record;       // 기록 내용 (있는 경우)

    // 액션 가능 여부
    private boolean canCreate;           // 새 기록 생성 가능
    private boolean canEdit;             // 수정 가능

    // 사용자 메시지
    private String statusMessage;        // 상태 메시지
    private String actionMessage;        // 액션 안내 메시지
}