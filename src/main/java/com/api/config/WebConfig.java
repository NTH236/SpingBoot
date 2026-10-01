package com.api.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.api.security.AuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Chỉ định Middleware này bảo vệ API /hello
        registry.addInterceptor(authInterceptor).addPathPatterns("/hello").
        excludePathPatterns("/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html");
    }
}