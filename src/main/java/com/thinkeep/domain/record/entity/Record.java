package com.thinkeep.domain.record.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 일기 기록 엔티티
 * 기존 records 테이블 구조에 맞춰 생성
 */
@Entity
@Table(name = "records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    // JSON 형태로 모든 답변 저장
    @Column(name = "answers", columnDefinition = "TEXT")
    private String answers;

    // Q2 관련 구조화된 데이터
    @Column(name = "person_category", length = 100)
    private String personCategory;

    @Column(name = "person_name", columnDefinition = "TEXT")
    private String personName;

    @Column(name = "voice_text", columnDefinition = "TEXT")
    private String voiceText;

    @Column(name = "editable_text", columnDefinition = "TEXT")
    private String editableText;

    @Column(name = "submitted_answer", columnDefinition = "TEXT")
    private String submittedAnswer;

    // 시간 추적용 (자동 생성될 수도 있음)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === JPA 생명주기 메서드 ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }



    /**
     * Map 형태의 답변을 JSON 문자열로 변환하여 저장
     */
    public void setAnswersFromMap(Map<String, String> answersMap) {
        if (answersMap == null || answersMap.isEmpty()) {
            this.answers = "{}";
            return;
        }

        try {
            // 간단한 JSON 구성 (org.json 없이)
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String> entry : answersMap.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":")
                        .append("\"").append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
            json.append("}");
            this.answers = json.toString();
        } catch (Exception e) {
            this.answers = "{}";
        }
    }

    /**
     * JSON 문자열을 Map으로 파싱하여 반환
     */
    public Map<String, String> getAnswersAsMap() {
        Map<String, String> result = new java.util.HashMap<>();

        if (this.answers == null || this.answers.trim().isEmpty()) {
            return result;
        }

        try {
            // 간단한 JSON 파싱 (기본적인 케이스만)
            String json = this.answers.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1); // { } 제거

                String[] pairs = json.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");
                        result.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            // 파싱 실패시 빈 Map 반환
        }

        return result;
    }

    /**
     * 특정 질문의 답변만 조회
     */
    public String getAnswerByQuestion(String questionId) {
        Map<String, String> answersMap = getAnswersAsMap();
        return answersMap.getOrDefault(questionId, "");
    }

    /**
     * JSON 문자열 이스케이프 처리
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }



    /**
     * 오늘 기록인지 확인
     */
    public boolean isToday() {
        return this.date != null && this.date.equals(LocalDate.now());
    }

    /**
     * 모든 필수 답변이 완료되었는지 확인
     */
    public boolean isComplete() {
        Map<String, String> answersMap = getAnswersAsMap();

        return isNotEmpty(answersMap.get("Q1")) &&
                isNotEmpty(answersMap.get("Q2")) &&
                isNotEmpty(answersMap.get("Q3")) &&
                isNotEmpty(answersMap.get("Q4"));
    }

    /**
     * 답변한 질문 수 계산
     */
    public int getAnswerCount() {
        Map<String, String> answersMap = getAnswersAsMap();
        int count = 0;

        if (isNotEmpty(answersMap.get("Q1"))) count++;
        if (isNotEmpty(answersMap.get("Q2"))) count++;
        if (isNotEmpty(answersMap.get("Q3"))) count++;
        if (isNotEmpty(answersMap.get("Q4"))) count++;

        return count;
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 기본 정보 출력 (디버깅용)
     */
    @Override
    public String toString() {
        return String.format("Record{id=%d, userNo=%d, date=%s, answerCount=%d}",
                recordId, userNo, date, getAnswerCount());
    }
}