package com.product_tracker.product_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.product_tracker.product_tracker.bot.TelegramBotService;
import com.product_tracker.product_tracker.config.ProductTrackerProperties;
import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.event.ProductStatusChangedEvent;
import com.product_tracker.product_tracker.repository.ProductRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ProductCheckerService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductTrackerProperties properties;
    private final TelegramBotService telegramBotService;

    public ProductCheckerService(
            ProductRepository productRepository,
            ApplicationEventPublisher eventPublisher,
            ProductTrackerProperties properties,
            TelegramBotService telegramBotService
    ) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.telegramBotService = telegramBotService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkProductStatus() {

        for (var productConfig : properties.getProducts()) {
            checkSingleProduct(productConfig);
        }
    }

    private void checkSingleProduct(ProductTrackerProperties.ProductConfig config) {
        try {
            log.info("🔁 Checking product via Shopify API..... {}", config.getName());
            String apiUrl = config.getUrl() + ".js";

            Document doc = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .get();
            String json = doc.body().text();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            boolean available = root.path("available").asBoolean();
            String publishedAt = root.path("published_at").asText();

            String buttonText;

            if (publishedAt == null || publishedAt.isEmpty()) {
                buttonText = "Coming Soon";
            } else if (available) {
                buttonText = "Add to Cart";
            } else {
                buttonText = "Sold Out";
            }

            log.info("Availability: {}", available);

            String statusMessage = "Status: " + buttonText +
                    "\nAvailable: " + (available ? "✅ Yes" : "❌ No");

            telegramBotService.updateLastButtonText(statusMessage);

            // ===== OPTIONAL: trigger notification if available =====
            if (available) {
                telegramBotService.broadcastMessage(
                        "🚨 Product Available!\n" + config.getName() +
                                "\nBuy now: " + config.getUrl()
                );
            }

        } catch (Exception e) {
            log.error("Error checking {}: {}", config.getName(), e.getMessage(), e);
        }
    }
}


