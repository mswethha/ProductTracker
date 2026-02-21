package com.product_tracker.product_tracker.service;

import com.product_tracker.product_tracker.domain.AvailabilityStatus;
import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    public ProductEntity getOrCreateProduct(String name, String url) {
        return productRepository.findByUrl(url)
                .orElseGet(() -> productRepository.save(
                        new ProductEntity(
                                null,
                                name,
                                url,
                                AvailabilityStatus.UNKNOWN,
                                LocalDateTime.now(),
                                null
                        )
                ));
    }

    /** True when availability status actually changes (for Telegram notifications). */
    public boolean didStatusChange(ProductEntity product, boolean available) {
        AvailabilityStatus current = product.getAvailabilityStatus();
        if (current == null || current == AvailabilityStatus.UNKNOWN) return true;
        return (available && current != AvailabilityStatus.IN_STOCK)
                || (!available && current == AvailabilityStatus.IN_STOCK);
    }

    public void updateStatus(ProductEntity product, boolean available) {
        product.updateAvailability(available);
        productRepository.save(product);
    }
}