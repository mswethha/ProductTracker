package com.product_tracker.product_tracker.repository;

import com.product_tracker.product_tracker.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository  extends JpaRepository<ProductEntity,Long> {

    Optional<ProductEntity> findByUrl(String url);
}
