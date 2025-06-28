package com.thinkeep.global.jwt;

import com.thinkeep.domain.user.dto.CreateRequest;
import com.thinkeep.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;
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

    /**
     * 서명용 키 생성 (내부 메서드)
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}