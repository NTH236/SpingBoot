package com.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Cấu hình tài liệu Swagger UI / OpenAPI cho Product Management Service.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SOA Product Management Service API")
                        .version("1.0.0")
                        .description("Tài liệu kỹ thuật RESTful API cho Dịch vụ Quản lý Sản phẩm (Product Service - Port 8081). "
                                + "Dịch vụ tự động xác thực token bằng cách gọi sang Auth Service (Port 8080). "
                                + "Hãy đăng nhập tại http://localhost:8080/auth/login để lấy token rồi nhập vào nút Authorize.")
                        .contact(new Contact().name("SOA Product Team").email("product-dev@soa-example.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập chuỗi JWT Token nhận được từ Auth Service (Port 8080)")));
    }
}
