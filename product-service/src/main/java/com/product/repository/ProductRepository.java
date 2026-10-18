package com.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.product.entity.Product;

/**
 * Giao diện Repository quản lý dữ liệu cho thực thể Product trong product_db.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}
