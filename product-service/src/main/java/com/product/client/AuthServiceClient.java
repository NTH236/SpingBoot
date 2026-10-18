package com.product.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.product.client.dto.TokenValidationResponse;

/**
 * Client giao tiếp liên dịch vụ (Inter-Service REST Client).
 * product-service sử dụng client này để gọi qua giao thức HTTP REST tới auth-service
 * (mặc định tại http://localhost:8080) nhằm kiểm tra tính hợp lệ của JWT Token.
 */
@Service
public class AuthServiceClient {

    private final RestClient restClient;
    private final String authServiceUrl;

    public AuthServiceClient(
            @Value("${auth.service.url:http://localhost:8080}") String authServiceUrl,
            @Value("${auth.service.validate-endpoint:/auth/validate}") String validateEndpoint) {
        this.authServiceUrl = authServiceUrl;
        this.restClient = RestClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    /**
     * Gửi yêu cầu HTTP GET tới auth-service để kiểm tra token.
     *
     * @param token Chuỗi JWT Token cần kiểm tra
     * @return TokenValidationResponse kết quả xác thực
     */
    public TokenValidationResponse validateToken(String token) {
        try {
            return restClient.get()
                    .uri("/auth/validate")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        // Nhận 401 hoặc 400 khi token không hợp lệ
                    })
                    .body(TokenValidationResponse.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .username(null)
                    .message("Token không hợp lệ hoặc đã hết hạn.")
                    .build();
        } catch (ResourceAccessException e) {
            // Không thể kết nối tới auth-service (auth-service chưa khởi động hoặc rớt mạng)
            throw new AuthServiceUnavailableException(
                    "Không thể kết nối tới Auth Service tại " + authServiceUrl + ". Vui lòng kiểm tra cổng 8080.", e);
        } catch (Exception e) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .username(null)
                    .message("Xác thực thất bại: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Ngoại lệ xảy ra khi auth-service không thể truy cập được.
     */
    public static class AuthServiceUnavailableException extends RuntimeException {
        public AuthServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
