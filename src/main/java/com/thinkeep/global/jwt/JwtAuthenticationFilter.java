package com.thinkeep.global.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청에서 JWT 토큰 추출
        String token = extractTokenFromRequest(request);

        // 2. 토큰이 있고 유효한지 검증
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            try {
                // 3. 토큰에서 사용자 정보 추출
                Long userNo = jwtUtil.getUserNoFromToken(token);
                String nickname = jwtUtil.getNicknameFromToken(token);

                // 4. Spring Security 인증 객체 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userNo,                     // principal (사용자 식별자)
                        null,                       // credentials (비밀번호 불필요)
                        Collections.emptyList()     // authorities (권한 - 일단 빈 리스트)
                );

                // 5. SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: userNo={}, nickname={}", userNo, nickname);

            } catch (Exception e) {
                log.warn("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        // 6. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // "Bearer " 제거
        }

        return null;
    }

    /**
     * 특정 경로는 JWT 검증을 건너뛸지 결정
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 로그인, 회원가입 경로는 JWT 검증 제외
        return path.startsWith("/api/auth/") ||
                (path.equals("/api/users") && "POST".equals(request.getMethod()));
    }
}
