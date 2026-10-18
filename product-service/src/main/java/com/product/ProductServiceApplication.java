package com.product;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.product.entity.Product;
import com.product.repository.ProductRepository;

/**
 * Điểm khởi chạy của Dịch vụ Quản lý Sản phẩm (Product Management Service).
 * Chạy độc lập trên cổng 8081 theo kiến trúc SOA.
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    /**
     * Khởi tạo một số sản phẩm mẫu trong CSDL product_db nếu cơ sở dữ liệu trống.
     */
    @Bean
    public CommandLineRunner initDefaultProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(Product.builder()
                        .name("Laptop Dell XPS 15")
                        .description("Laptop doanh nhân cao cấp màn hình OLED 3.5K")
                        .price(new BigDecimal("35000000.00"))
                        .quantity(10)
                        .build());
                productRepository.save(Product.builder()
                        .name("Bàn phím cơ Keychron Q1 Pro")
                        .description("Bàn phím không dây vỏ nhôm CNC kết nối đa thiết bị")
                        .price(new BigDecimal("4200000.00"))
                        .quantity(20)
                        .build());
                productRepository.save(Product.builder()
                        .name("Chuột Logitech MX Master 3S")
                        .description("Chuột công thái học cao cấp với con lăn siêu tốc MagSpeed")
                        .price(new BigDecimal("2250000.00"))
                        .quantity(15)
                        .build());
            }
        };
    }
}
