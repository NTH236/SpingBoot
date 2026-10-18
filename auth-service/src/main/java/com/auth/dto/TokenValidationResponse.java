package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi kết quả kiểm tra token cho các dịch vụ khác (như product-service) trong kiến trúc SOA.
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
