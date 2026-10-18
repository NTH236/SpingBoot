package com.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.AuthResponse;
import com.auth.dto.LoginRequest;
import com.auth.dto.RegisterRequest;
import com.auth.dto.TokenValidationResponse;
import com.auth.entity.User;
import com.auth.repository.UserRepository;
import com.auth.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller xử lý các yêu cầu Xác thực và Quản lý Người dùng trong dịch vụ auth-service.
 * Chạy độc lập trên cổng mạng 8080.
 * Cung cấp API /auth/validate cho các dịch vụ khác (như product-service) giao tiếp qua HTTP REST.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Service", description = "Các API xác thực và cấp phát/kiểm tra Token (SOA Service trên Port 8080)")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /**
     * API Đăng nhập cấp phát JWT Token.
     * Hỗ trợ cả RequestBody JSON và Form RequestParam (tương thích ngược).
     */
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Xác thực thông tin tài khoản và cấp JWT Token.")
    public ResponseEntity<?> login(
            @RequestBody(required = false) LoginRequest request,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String password) {

        String inputUser = (request != null && request.getUserName() != null) ? request.getUserName() : userName;
        String inputPass = (request != null && request.getPassword() != null) ? request.getPassword() : password;

        if (inputUser == null || inputPass == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("message", "Vui lòng nhập tên đăng nhập và mật khẩu.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Kiểm tra thông tin trong CSDL auth_db
        boolean isAuthenticated = userRepository.findByUsername(inputUser)
                .map(user -> user.getPassword().equals(inputPass))
                .orElse(false);

        // Hỗ trợ tài khoản mẫu mặc định (hoang / 123456)
        if (!isAuthenticated && "hoang".equals(inputUser) && "123456".equals(inputPass)) {
            isAuthenticated = true;
        }

        if (isAuthenticated) {
            String token = jwtUtil.generateToken(inputUser);
            // Trả về cả token plain text và đối tượng AuthResponse để tương thích
            return ResponseEntity.ok(token);
        }

        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.UNAUTHORIZED.value());
        error.put("error", "Unauthorized");
        error.put("message", "Truy cập bị từ chối. Sai thông tin đăng nhập.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * API Đăng ký người dùng mới.
     */
    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo mới một tài khoản người dùng trong CSDL auth_db.")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUserName() == null || request.getUserName().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("message", "Tên đăng nhập và mật khẩu không được để trống.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (userRepository.existsByUsername(request.getUserName())) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.CONFLICT.value());
            error.put("message", "Tên tài khoản đã tồn tại trên hệ thống.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        User user = User.builder()
                .username(request.getUserName())
                .password(request.getPassword())
                .email(request.getEmail())
                .role("USER")
                .build();
        userRepository.save(user);

        Map<String, Object> success = new HashMap<>();
        success.put("status", HttpStatus.CREATED.value());
        success.put("message", "Đăng ký tài khoản thành công.");
        success.put("username", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(success);
    }

    /**
     * API Giao tiếp Liên dịch vụ (Inter-Service REST API Endpoint):
     * Cho phép các dịch vụ khác trong hệ thống SOA (như product-service trên port 8081)
     * gọi qua HTTP để xác thực tính hợp lệ của token và lấy thông tin người dùng.
     */
    @GetMapping("/validate")
    @Operation(summary = "Xác thực Token (Inter-Service Endpoint)",
               description = "Endpoint dùng cho các service khác gọi tới để kiểm tra tính hợp lệ của Token.")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam) {

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            token = tokenParam;
        }

        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenValidationResponse.builder()
                            .valid(false)
                            .username(null)
                            .message("Token không hợp lệ hoặc đã hết hạn.")
                            .build());
        }

        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(TokenValidationResponse.builder()
                .valid(true)
                .username(username)
                .message("Token hợp lệ.")
                .build());
    }
}
