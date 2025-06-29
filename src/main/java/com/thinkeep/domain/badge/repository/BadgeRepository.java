package com.thinkeep.domain.badge.repository;

import com.thinkeep.domain.badge.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * 뱃지 데이터 접근 인터페이스
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    /**
     * 이름으로 뱃지 조회
     * @param name 뱃지 이름
     * @return 뱃지 정보
     */
    Optional<Badge> findByName(String name);

    /**
     * 이름 중복 확인
     * @param name 뱃지 이름
     * @return 존재 여부
     */
    boolean existsByName(String name);

    /**
     * 조건에 포함된 문자열로 뱃지 검색
     * @param keyword 조건 키워드
     * @return 조건 포함된 뱃지 리스트
     */
    List<Badge> findByConditionJsonContaining(String keyword);


}
