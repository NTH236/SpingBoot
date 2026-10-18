package com.product.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.product.entity.Product;
import com.product.repository.ProductRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller xử lý các yêu cầu RESTful API cho Dịch vụ Quản lý Sản phẩm (Product Management Service).
 * Chạy độc lập trên cổng mạng riêng biệt 8081.
 * Bảo vệ bởi AuthInterceptor thông qua giao tiếp liên dịch vụ với Auth Service (Port 8080).
 */
@RestController
@RequestMapping("/products")
@Tag(name = "Product Management Service", description = "Các API quản lý sản phẩm (SOA Service trên Port 8081)")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * API Lấy danh sách tất cả các sản phẩm.
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả sản phẩm", description = "Truy vấn danh sách toàn bộ sản phẩm hiện có trong CSDL product_db.")
    @ApiResponse(responseCode = "200", description = "Truy xuất danh sách thành công")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    /**
     * API Lấy thông tin chi tiết một sản phẩm theo ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết một sản phẩm", description = "Tìm kiếm và trả về thông tin của một sản phẩm theo mã ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tìm thấy sản phẩm"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm với ID cung cấp")
    })
    public ResponseEntity<?> getProductById(
            @Parameter(description = "Mã ID sản phẩm cần tìm", required = true)
            @PathVariable Integer id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok((Object) product))
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                    errorResponse.put("error", "Not Found");
                    errorResponse.put("message", "Không tìm thấy sản phẩm với ID: " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    /**
     * API Thêm mới một sản phẩm vào hệ thống.
     */
    @PostMapping
    @Operation(summary = "Thêm một sản phẩm mới", description = "Tạo mới một sản phẩm và lưu vào cơ sở dữ liệu product_db.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tạo sản phẩm mới thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ")
    })
    public ResponseEntity<?> createProduct(
            @RequestBody Product product,
            @RequestAttribute(value = "authenticatedUser", required = false) String currentUser) {

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("message", "Tên sản phẩm không được để trống.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (product.getPrice() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("message", "Giá sản phẩm không được để trống.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (product.getQuantity() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("message", "Số lượng sản phẩm không được để trống.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        product.setId(null);
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    /**
     * API Cập nhật thông tin một sản phẩm theo ID.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin sản phẩm", description = "Cập nhật thông tin chi tiết của một sản phẩm hiện có.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật sản phẩm thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm cần cập nhật")
    })
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "Mã ID sản phẩm cần cập nhật", required = true)
            @PathVariable Integer id,
            @RequestBody Product productDetails) {

        return productRepository.findById(id)
                .map(existingProduct -> {
                    if (productDetails.getName() != null && !productDetails.getName().trim().isEmpty()) {
                        existingProduct.setName(productDetails.getName());
                    }
                    if (productDetails.getDescription() != null) {
                        existingProduct.setDescription(productDetails.getDescription());
                    }
                    if (productDetails.getPrice() != null) {
                        existingProduct.setPrice(productDetails.getPrice());
                    }
                    if (productDetails.getQuantity() != null) {
                        existingProduct.setQuantity(productDetails.getQuantity());
                    }

                    Product updatedProduct = productRepository.save(existingProduct);
                    return ResponseEntity.ok((Object) updatedProduct);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                    errorResponse.put("error", "Not Found");
                    errorResponse.put("message", "Không tìm thấy sản phẩm để cập nhật với ID: " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    /**
     * API Xóa một sản phẩm theo ID.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một sản phẩm", description = "Xóa một sản phẩm khỏi CSDL product_db theo mã ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xóa sản phẩm thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm cần xóa")
    })
    public ResponseEntity<?> deleteProduct(
            @Parameter(description = "Mã ID sản phẩm cần xóa", required = true)
            @PathVariable Integer id) {

        return productRepository.findById(id)
                .map(existingProduct -> {
                    productRepository.delete(existingProduct);
                    Map<String, Object> successResponse = new HashMap<>();
                    successResponse.put("status", HttpStatus.OK.value());
                    successResponse.put("message", "Đã xóa thành công sản phẩm có ID: " + id);
                    return ResponseEntity.ok((Object) successResponse);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                    errorResponse.put("error", "Not Found");
                    errorResponse.put("message", "Không tìm thấy sản phẩm để xóa với ID: " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }
}
