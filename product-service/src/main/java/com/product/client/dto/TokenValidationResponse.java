package com.product.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận phản hồi từ API /auth/validate của auth-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {
    private boolean valid;
    private String username;
    private String message;
}
