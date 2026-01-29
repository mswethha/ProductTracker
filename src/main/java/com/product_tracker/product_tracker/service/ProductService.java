package com.product_tracker.product_tracker.service;

import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.product_tracker.product_tracker.domain.AvailabilityStatus;
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
                                "UNKNOWN",
                                LocalDateTime.now()
                        )
                ));
    }

    public boolean isStatusChanged(ProductEntity product, String newButtonText) {
        return !newButtonText.equalsIgnoreCase(product.getButtonText());
    }

    public void updateStatus(ProductEntity product, String buttonText) {
        product.updateFromButtonText(buttonText);
        productRepository.save(product);
    }
}