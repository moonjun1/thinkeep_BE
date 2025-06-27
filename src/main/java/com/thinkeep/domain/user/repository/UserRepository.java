package com.thinkeep.domain.user.repository;



import com.thinkeep.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 데이터 접근 인터페이스
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 닉네임으로 사용자 조회
     * @param nickname 닉네임
     * @return 사용자 정보
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 카카오 ID로 사용자 조회
     * @param kakaoId 카카오 ID
     * @return 사용자 정보
     */
    Optional<User> findByKakaoId(String kakaoId);

    /**
     * 닉네임 중복 확인
     * @param nickname 닉네임
     * @return 존재 여부
     */
    boolean existsByNickname(String nickname);

    /**
     * 카카오 ID 중복 확인
     * @param kakaoId 카카오 ID
     * @return 존재 여부
     */
    boolean existsByKakaoId(String kakaoId);

    /**
     * 닉네임으로 사용자 삭제
     * @param nickname 닉네임
     */
    void deleteByNickname(String nickname);

    /**
     * 스트릭 카운트가 특정 값 이상인 사용자 수 조회
     * @param streakCount 스트릭 카운트
     * @return 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.streakCount >= :streakCount")
    long countByStreakCountGreaterThanEqual(@Param("streakCount") Integer streakCount);
}
