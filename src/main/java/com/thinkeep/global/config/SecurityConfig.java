package com.thinkeep.global.config;

import com.thinkeep.global.jwt.JwtAuthenticationFilter;
import com.thinkeep.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    //  JWT 토글 설정
    @Value("${app.security.jwt-enabled:true}")
    private boolean jwtEnabled;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 🎯 현재 JWT 상태 로그 출력
        if (jwtEnabled) {
            log.info("🔐 JWT 인증 활성화됨");
        } else {
            log.warn("🔓 JWT 인증 비활성화됨 - 개발용입니다!");
        }

        // 기본 설정
        HttpSecurity httpSecurity = http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // JWT 토글에 따른 권한 설정
        if (jwtEnabled) {
            // 🔒 JWT 인증 모드
            httpSecurity.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/**").permitAll()                   // 🟢 Actuator 허용
                            .requestMatchers("/api/auth/**").permitAll()                   // 로그인, 카카오 로그인
                            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()    // 회원가입
                            .anyRequest().authenticated()                                  // 그 외 모든 요청은 인증 필요
                    )
                    .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        } else {
            // 🔓 개발 모드 - 모든 요청 허용
            httpSecurity.authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            );
        }

        return httpSecurity.build();
    }

    /**
     * AWS EC2 배포용 완전한 CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔧 모든 EC2 주소 형태 허용
        List<String> allowedOrigins = new ArrayList<>();

        // 로컬 개발 환경
        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("http://localhost:8080");
        allowedOrigins.add("http://127.0.0.1:3000");
        allowedOrigins.add("http://127.0.0.1:8080");

        // AWS EC2 - IP 주소 방식
        allowedOrigins.add("http://13.209.69.235");
        allowedOrigins.add("http://13.209.69.235:8080");
        allowedOrigins.add("http://13.209.69.235:3000");
        allowedOrigins.add("http://13.209.69.235:9090");

        // AWS EC2 - 도메인 방식
        allowedOrigins.add("http://ec2-13-209-69-235.ap-northeast-2.compute.amazonaws.com");
        allowedOrigins.add("http://ec2-13-209-69-235.ap-northeast-2.compute.amazonaws.com:8080");
        allowedOrigins.add("http://ec2-13-209-69-235.ap-northeast-2.compute.amazonaws.com:3000");
        allowedOrigins.add("http://ec2-13-209-69-235.ap-northeast-2.compute.amazonaws.com:9090");

        // 🚀 추후 HTTPS 및 커스텀 도메인 (예정)
        allowedOrigins.add("https://13.209.69.235");
        allowedOrigins.add("https://ec2-13-209-69-235.ap-northeast-2.compute.amazonaws.com");

        configuration.setAllowedOrigins(allowedOrigins);

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 응답 헤더 노출
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
        ));

        // 🔧 credentials 허용 (JWT 토큰용)
        configuration.setAllowCredentials(true);

        // 🔧 Preflight 캐싱 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("🌍 CORS 설정 완료 - 허용 도메인 수: {}", allowedOrigins.size());
        log.debug("🔍 허용 도메인 목록: {}", allowedOrigins);

        return source;
    }
}