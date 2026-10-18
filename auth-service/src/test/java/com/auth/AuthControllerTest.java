package com.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.auth.dto.LoginRequest;
import com.auth.dto.RegisterRequest;
import com.auth.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Kiểm thử tích hợp cho AuthController trong auth-service (Port 8080).
 */
@SpringBootTest
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Kiểm tra đăng nhập thành công với tài khoản mặc định (POST /auth/login - 200 OK)")
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("hoang", "123456");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())));
    }

    @Test
    @DisplayName("Kiểm tra đăng nhập thất bại với sai mật khẩu (POST /auth/login - 401 Unauthorized)")
    void testLoginFailure() throws Exception {
        LoginRequest loginRequest = new LoginRequest("hoang", "wrongpass");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Kiểm tra đăng ký tài khoản mới thành công (POST /auth/register - 201 Created)")
    void testRegisterSuccess() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("testuser_" + System.currentTimeMillis(), "password123", "test@soa.com");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    @DisplayName("Kiểm tra Inter-Service API xác thực token hợp lệ (GET /auth/validate - 200 OK)")
    void testValidateTokenValid() throws Exception {
        String token = jwtUtil.generateToken("hoang");

        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("hoang"));
    }

    @Test
    @DisplayName("Kiểm tra Inter-Service API từ chối token không hợp lệ (GET /auth/validate - 401 Unauthorized)")
    void testValidateTokenInvalid() throws Exception {
        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer invalid-token-string"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
