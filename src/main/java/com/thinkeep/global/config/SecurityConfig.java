package com.thinkeep.global.config;

import com.thinkeep.global.jwt.JwtAuthenticationFilter;
import com.thinkeep.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
            log.info(" JWT 인증 활성화됨");
        } else {
            log.warn(" JWT 인증 비활성화됨 - 개발용입니다!");
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
                            .requestMatchers("/api/auth/**").permitAll()          // 로그인, 카카오 로그인
                            .requestMatchers("POST", "/api/users").permitAll()    // 회원가입
                            .anyRequest().authenticated()                         // 그 외 모든 요청은 인증 필요
                    )
                    .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        } else {
            // 개발 모드 - 모든 요청 허용
            httpSecurity.authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            );
        }

        return httpSecurity.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 도메인 (개발 시에는 모든 도메인 허용)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}