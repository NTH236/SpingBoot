package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về kết quả đăng nhập / xác thực chứa JWT Token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private String userName;
    private String message;

    public static AuthResponse of(String token, String userName) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userName(userName)
                .message("Đăng nhập thành công")
                .build();
    }
}
