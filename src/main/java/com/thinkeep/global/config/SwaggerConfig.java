package com.thinkeep.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement())
                .components(components());
    }

    private Info apiInfo() {
        return new Info()
                .title("Thinkeep API")
                .description("일기 작성 및 관리 API 문서")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Thinkeep Team")
                        .email("contact@thinkeep.com"));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("JWT");
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes("JWT", new SecurityScheme()
                        .name("JWT")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다."));
    }
}