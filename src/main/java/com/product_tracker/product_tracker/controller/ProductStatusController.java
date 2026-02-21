package com.product_tracker.product_tracker.controller;

import com.product_tracker.product_tracker.config.ProductTrackerProperties;
import com.product_tracker.product_tracker.domain.AvailabilityStatus;
import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.service.ProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets authenticated users (subscribers) view tracked product status.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductStatusController {

    private final ProductService productService;
    private final ProductTrackerProperties properties;

    @GetMapping("/status")
    public ResponseEntity<List<ProductStatusDto>> getProductStatus(
            @AuthenticationPrincipal UserDetails user) {
        List<ProductStatusDto> dtos = new ArrayList<>();
        for (var config : properties.getProducts()) {
            ProductEntity product = productService.getOrCreateProduct(config.getName(), config.getUrl().trim());
            dtos.add(toDto(product));
        }
        return ResponseEntity.ok(dtos);
    }

    private static ProductStatusDto toDto(ProductEntity p) {
        ProductStatusDto dto = new ProductStatusDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setUrl(p.getUrl());
        dto.setStatus(p.getAvailabilityStatus() == null ? AvailabilityStatus.UNKNOWN : p.getAvailabilityStatus());
        dto.setLastChecked(p.getLastChecked());
        dto.setStatusMessage(statusMessage(p.getAvailabilityStatus()));
        return dto;
    }

    private static String statusMessage(AvailabilityStatus status) {
        if (status == null) return "Unknown";
        return switch (status) {
            case IN_STOCK -> "In stock";
            case OUT_OF_STOCK -> "Out of stock";
            case UNKNOWN -> "Not checked yet";
        };
    }

    @Data
    public static class ProductStatusDto {
        private Long id;
        private String name;
        private String url;
        private AvailabilityStatus status;
        private String statusMessage;
        private java.time.LocalDateTime lastChecked;
    }
}
