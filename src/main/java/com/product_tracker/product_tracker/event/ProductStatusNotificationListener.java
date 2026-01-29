package com.product_tracker.product_tracker.event;

import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.event.ProductStatusChangedEvent;
import com.product_tracker.product_tracker.bot.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductStatusNotificationListener {

    private final TelegramBotService telegramBotService;

    public ProductStatusNotificationListener(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @EventListener
    public void handleProductStatusChange(ProductStatusChangedEvent event) {

        ProductEntity product = event.product();

        String message =
                product.getAvailabilityStatus().equals("AVAILABLE")
                        ? "Product is AVAILABLE! " + product.getUrl()
                        : "Product status changed: " + product.getButtonText();

        telegramBotService.broadcastMessage(message);

        log.info("Notification sent for product {}", product.getName());
    }
}