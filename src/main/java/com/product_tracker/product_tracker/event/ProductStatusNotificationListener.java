package com.product_tracker.product_tracker.event;

import com.product_tracker.product_tracker.entity.ProductEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductStatusNotificationListener {

    @EventListener
    public void handleProductStatusChange(ProductEntity product) {
        log.info("🔔 Product status changed!");
        log.info("Product: {}", product.getName());
        log.info("Availability: {}", product.getAvailabilityStatus());
        log.info("URL: {}", product.getUrl());
    }
}