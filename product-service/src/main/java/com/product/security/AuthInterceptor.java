package com.product.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.product.client.AuthServiceClient;
import com.product.client.AuthServiceClient.AuthServiceUnavailableException;
import com.product.client.dto.TokenValidationResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor bảo vệ các endpoint của product-service.
 * Thực hiện gọi liên dịch vụ sang auth-service qua AuthServiceClient để xác thực Bearer Token.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthServiceClient authServiceClient;

    public AuthInterceptor(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                TokenValidationResponse validationResponse = authServiceClient.validateToken(token);

                if (validationResponse != null && validationResponse.isValid()) {
                    // Lưu định danh người dùng đã xác thực vào request để Controller có thể sử dụng
                    request.setAttribute("authenticatedUser", validationResponse.getUsername());
                    return true;
                }
            } catch (AuthServiceUnavailableException e) {
                // Phản hồi mã lỗi HTTP 503 khi dịch vụ auth-service chưa chạy hoặc rớt kết nối
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"status\": 503, \"error\": \"Service Unavailable\", \"message\": \"" + e.getMessage() + "\"}");
                return false;
            }
        }

        // Phản hồi mã lỗi HTTP 401 Unauthorized khi thiếu hoặc token không hợp lệ
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Khong tim thay chung chi xac thuc hop le hoac token da het han.\"}");
        return false;
    }
}
