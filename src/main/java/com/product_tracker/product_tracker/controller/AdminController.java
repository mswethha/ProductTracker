package com.product_tracker.product_tracker.controller;

import com.product_tracker.product_tracker.config.ProductTrackerProperties;
import com.product_tracker.product_tracker.domain.AvailabilityStatus;
import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.repository.UserRepository;
import com.product_tracker.product_tracker.service.ProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final UserRepository userRepository; // replaces SubscriberService
    private final ProductTrackerProperties properties;

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminProductDto>> getProducts() {
        List<AdminProductDto> dtos = new ArrayList<>();
        for (var config : properties.getProducts()) {
            ProductEntity p = productService.getOrCreateProduct(
                    config.getName(), config.getUrl().trim());
            dtos.add(toDto(p));
        }
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/subscribers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriberDto>> getSubscribers() {
        // Now returns users who have linked their Telegram
        List<SubscriberDto> dtos = userRepository.findAll().stream()
                .filter(u -> u.getTelegramChatId() != null
                        && !u.getTelegramChatId().isBlank())
                .map(u -> {
                    SubscriberDto d = new SubscriberDto();
                    d.setId(u.getId());
                    d.setChatId(u.getTelegramChatId());
                    d.setUsername(u.getUsername()); // bonus: show username too
                    return d;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private static AdminProductDto toDto(ProductEntity p) {
        AdminProductDto d = new AdminProductDto();
        d.setId(p.getId());
        d.setName(p.getName());
        d.setUrl(p.getUrl());
        d.setStatus(p.getAvailabilityStatus() == null
                ? AvailabilityStatus.UNKNOWN : p.getAvailabilityStatus());
        d.setLastChecked(p.getLastChecked());
        d.setLastInStockAt(p.getLastInStockAt());
        d.setStatusMessage(p.getAvailabilityStatus() == AvailabilityStatus.IN_STOCK
                ? "Available" : "Out of stock");
        return d;
    }

    @Data
    public static class AdminProductDto {
        private Long id;
        private String name;
        private String url;
        private AvailabilityStatus status;
        private String statusMessage;
        private java.time.LocalDateTime lastChecked;
        private java.time.LocalDateTime lastInStockAt;
    }

    @Data
    public static class SubscriberDto {
        private Long id;
        private String chatId;
        private String username;
    }
}