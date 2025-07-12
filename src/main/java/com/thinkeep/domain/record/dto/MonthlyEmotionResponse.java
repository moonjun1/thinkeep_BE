package com.thinkeep.domain.record.dto;

import lombok.*;

import java.util.Map;

/**
 * 월별 감정 데이터 응답 DTO
 * 캘린더에서 월별 감정 표시용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyEmotionResponse {

    /**
     * 사용자 번호
     */
    private Long userNo;

    /**
     * 조회 연도
     */
    private Integer year;

    /**
     * 조회 월
     */
    private Integer month;

    /**
     * 날짜별 감정 맵
     * 예시: {"2025-07-01": "happy", "2025-07-02": "gloomy"}
     */
    private Map<String, String> emotions;

    /**
     * 해당 월 총 기록 수
     */
    private Integer totalRecords;

    /**
     * 감정별 통계
     * 예시: {"happy": 5, "sad": 2, "angry": 1}
     */
    private Map<String, Integer> emotionStats;

    /**
     * 가장 많이 나타난 감정
     */
    private String dominantEmotion;

    /**
     * 응답 생성 시간
     */
    private String timestamp;
} 