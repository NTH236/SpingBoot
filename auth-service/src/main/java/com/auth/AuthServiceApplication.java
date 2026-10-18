package com.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.auth.entity.User;
import com.auth.repository.UserRepository;

/**
 * Điểm khởi chạy của Dịch vụ Xác thực người dùng (Auth Service).
 * Chạy độc lập trên cổng 8080 theo kiến trúc SOA.
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    /**
     * Khởi tạo tài khoản mẫu (hoang / 123456) trong CSDL auth_db nếu chưa tồn tại.
     */
    @Bean
    public CommandLineRunner initDefaultUser(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByUsername("hoang")) {
                userRepository.save(User.builder()
                        .username("hoang")
                        .password("123456")
                        .email("hoang@soa-example.com")
                        .role("ADMIN")
                        .build());
            }
        };
    }
}
