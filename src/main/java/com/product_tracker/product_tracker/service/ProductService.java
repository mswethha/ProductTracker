package com.product_tracker.product_tracker.service;

import com.product_tracker.product_tracker.domain.AvailabilityStatus;
import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductEntity getOrCreateProduct(String name, String url) {
        return productRepository.findByUrl(url)
                .orElseGet(() -> productRepository.save(
                        new ProductEntity(
                                null,
                                name,
                                url,
                                AvailabilityStatus.UNKNOWN,
                                LocalDateTime.now()
                        )
                ));
    }

    public boolean shouldNotify(ProductEntity product, boolean available) {
        // Notify only when product becomes available
        return product.getAvailabilityStatus() != AvailabilityStatus.IN_STOCK && available;
    }

    public void updateStatus(ProductEntity product, boolean available) {
        product.updateAvailability(available);
        productRepository.save(product);
    }
}