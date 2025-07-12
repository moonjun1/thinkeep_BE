package com.thinkeep.global.jwt;

import com.thinkeep.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.accessTokenExpirationTime}")
    private long accessTokenExpirationTime;

    @Value("${jwt.secretKey}")
    private String secretKey;

    /**
     * JWT 토큰 생성
     */
    public String generateToken(User user) {
        // 🔍 디버깅 로그 추가
        log.info("🔍 JWT 토큰 생성 시작");
        log.info("🔍 설정된 secretKey: '{}'", secretKey);
        log.info("🔍 secretKey 길이: {}", secretKey != null ? secretKey.length() : "null");
        log.info("🔍 secretKey 바이트 길이: {}", secretKey != null ? secretKey.getBytes().length : "null");

        Map<String, Object> claims = new HashMap<>();
        claims.put("userNo", user.getUserNo());
        claims.put("nickname", user.getNickname());
        claims.put("isKakaoUser", user.getKakaoId() != null);

        return createToken(claims, user.getNickname());
    }

    /**
     * 토큰 생성 (내부 메서드)
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 서명용 키 생성 (내부 메서드) - 🚨 긴급 안전장치 추가
     */
    private SecretKey getSigningKey() {
        log.info("🔍 getSigningKey() 호출됨");
        log.info("🔍 현재 secretKey 값: '{}'", secretKey);

        // 🚨 긴급 안전장치: secretKey가 비어있거나 null이면 강제로 안전한 키 사용
        String actualKey = secretKey;

        if (actualKey == null || actualKey.trim().isEmpty()) {
            log.error("❌ JWT secretKey가 비어있습니다! 임시 키 사용");
            actualKey = "EmergencySecretKeyForJwtThatIsAtLeast32CharactersLongToEnsureSecurity123456789";
        }

        // 키 길이 재확인
        if (actualKey.getBytes().length < 32) {
            log.error("❌ JWT secretKey가 너무 짧습니다! ({} bytes) 임시 키 사용", actualKey.getBytes().length);
            actualKey = "EmergencySecretKeyForJwtThatIsAtLeast32CharactersLongToEnsureSecurity123456789";
        }

        log.info("✅ 최종 사용할 키: '{}' (길이: {})", actualKey, actualKey.length());

        return Keys.hmacShaKeyFor(actualKey.getBytes());
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 토큰입니다: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰에서 사용자 번호 추출
     */
    public Long getUserNoFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userNo", Long.class);
    }

    /**
     * 토큰에서 닉네임 추출
     */
    public String getNicknameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("nickname", String.class);
    }

    /**
     * 토큰에서 카카오 사용자 여부 추출
     */
    public Boolean getIsKakaoUserFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("isKakaoUser", Boolean.class);
    }

    /**
     * 토큰에서 만료시간 추출
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 토큰에서 Claims 추출 (내부 메서드)
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}