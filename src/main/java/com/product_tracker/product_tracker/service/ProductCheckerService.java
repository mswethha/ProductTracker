package com.product_tracker.product_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.product_tracker.product_tracker.bot.TelegramBotService;
import com.product_tracker.product_tracker.config.ProductTrackerProperties;
import com.product_tracker.product_tracker.entity.ProductEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductCheckerService {

    private final ProductService productService;
    private final ProductTrackerProperties properties;
    private final TelegramBotService telegramBotService;

    public ProductCheckerService(
            ProductService productService,
            ProductTrackerProperties properties,
            TelegramBotService telegramBotService
    ) {
        this.productService = productService;
        this.properties = properties;
        this.telegramBotService = telegramBotService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkProductStatus() {
        for (var config : properties.getProducts()) {
            checkSingleProduct(config);
        }
    }

    private void checkSingleProduct(ProductTrackerProperties.ProductConfig config) {
        try {
            log.info("Checking product: {}", config.getName());

            String apiUrl = config.getUrl() + ".js";

            Document doc = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .get();

            String json = doc.body().text();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            boolean available = root.path("available").asBoolean();

            ProductEntity product =
                    productService.getOrCreateProduct(config.getName(), config.getUrl().trim());

            boolean statusChanged = productService.didStatusChange(product, available);
            String productName = config.getName();
            String productUrl = config.getUrl().trim();

            // always update DB
            productService.updateStatus(product, available);

            // 🔔 notify subscribers on any status change
            if (statusChanged) {
                if (available) {
                    telegramBotService.broadcastMessage(
                            "🚨 PRODUCT AVAILABLE!\n" + productName + "\nBuy now: " + productUrl
                    );
                } else {
                    telegramBotService.broadcastMessage(
                            "📦 Product no longer in stock: " + productName
                    );
                }
            }

            telegramBotService.updateLastButtonText(
                    available ? "Available" : "Out of Stock"
            );

        } catch (Exception e) {
            log.error("Error checking {}: {}", config.getName(), e.getMessage());
        }
    }
}