package com.product;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.client.AuthServiceClient;
import com.product.client.AuthServiceClient.AuthServiceUnavailableException;
import com.product.client.dto.TokenValidationResponse;
import com.product.entity.Product;
import com.product.repository.ProductRepository;

/**
 * Kiểm thử tích hợp toàn diện cho Dịch vụ Quản lý Sản phẩm (Product Service - Port 8081).
 * Kiểm tra xác thực qua AuthServiceClient và các thao tác CRUD với CSDL product_db.
 */
@SpringBootTest
public class ProductControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String validToken = "Bearer test-valid-jwt-token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        productRepository.deleteAll();

        // Giả lập phản hồi xác thực thành công từ auth-service
        when(authServiceClient.validateToken("test-valid-jwt-token"))
                .thenReturn(TokenValidationResponse.builder()
                        .valid(true)
                        .username("hoang")
                        .message("Token hợp lệ")
                        .build());
    }

    @Test
    @DisplayName("Kiểm tra chặn truy cập khi không có Bearer Token (AuthInterceptor chặn - 401)")
    void testAccessDeniedWithoutToken() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Kiểm tra xử lý khi Auth Service không khả dụng (Cổng 8080 rớt - 503 Service Unavailable)")
    void testAuthServiceUnavailable() throws Exception {
        when(authServiceClient.validateToken(anyString()))
                .thenThrow(new AuthServiceUnavailableException("Cannot connect to Auth Service", null));

        mockMvc.perform(get("/products")
                .header("Authorization", validToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    @DisplayName("Kiểm tra thêm mới sản phẩm thành công (POST /products - 201 Created)")
    void testCreateProductSuccess() throws Exception {
        Product newProduct = Product.builder()
                .name("Laptop Dell XPS 15")
                .description("Laptop chuyên nghiệp cho lập trình viên")
                .price(new BigDecimal("35000000.00"))
                .quantity(10)
                .build();

        mockMvc.perform(post("/products")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Laptop Dell XPS 15"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("Kiểm tra lấy danh sách tất cả sản phẩm (GET /products - 200 OK)")
    void testGetAllProductsSuccess() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Bàn phím cơ Keychron")
                .description("Bàn phím không dây")
                .price(new BigDecimal("1850000.00"))
                .quantity(25)
                .build());

        mockMvc.perform(get("/products")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Bàn phím cơ Keychron"));
    }

    @Test
    @DisplayName("Kiểm tra lấy chi tiết sản phẩm theo ID (GET /products/{id} - 200 OK)")
    void testGetProductByIdSuccess() throws Exception {
        Product savedProduct = productRepository.save(Product.builder()
                .name("Chuột Logitech MX Master 3S")
                .description("Chuột công thái học cao cấp")
                .price(new BigDecimal("2200000.00"))
                .quantity(15)
                .build());

        mockMvc.perform(get("/products/" + savedProduct.getId())
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Chuột Logitech MX Master 3S"));
    }

    @Test
    @DisplayName("Kiểm tra lấy sản phẩm không tồn tại (GET /products/{id} - 404 Not Found)")
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/products/99999")
                .header("Authorization", validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Kiểm tra cập nhật thông tin sản phẩm (PUT /products/{id} - 200 OK)")
    void testUpdateProductSuccess() throws Exception {
        Product savedProduct = productRepository.save(Product.builder()
                .name("Màn hình Dell UltraSharp")
                .description("Màn hình 27 inch 4K")
                .price(new BigDecimal("12000000.00"))
                .quantity(5)
                .build());

        Product updateDetails = Product.builder()
                .name("Màn hình Dell UltraSharp U2723QE")
                .description("Màn hình 27 inch 4K IPS Black")
                .price(new BigDecimal("13500000.00"))
                .quantity(8)
                .build();

        mockMvc.perform(put("/products/" + savedProduct.getId())
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Màn hình Dell UltraSharp U2723QE"))
                .andExpect(jsonPath("$.price").value(13500000.00))
                .andExpect(jsonPath("$.quantity").value(8));
    }

    @Test
    @DisplayName("Kiểm tra xóa sản phẩm theo ID (DELETE /products/{id} - 200 OK)")
    void testDeleteProductSuccess() throws Exception {
        Product savedProduct = productRepository.save(Product.builder()
                .name("Tai nghe Sony WH-1000XM5")
                .description("Tai nghe chống ồn chủ động")
                .price(new BigDecimal("7990000.00"))
                .quantity(7)
                .build());

        mockMvc.perform(delete("/products/" + savedProduct.getId())
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        // Kiểm tra sau khi xóa thì truy vấn lại sẽ nhận 404
        mockMvc.perform(get("/products/" + savedProduct.getId())
                .header("Authorization", validToken))
                .andExpect(status().isNotFound());
    }
}
