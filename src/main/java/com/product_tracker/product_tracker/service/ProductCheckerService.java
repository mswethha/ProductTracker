package com.product_tracker.product_tracker.service;

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

    public ProductCheckerService(
            ProductRepository productRepository,
            ApplicationEventPublisher eventPublisher,
            ProductTrackerProperties properties
    ) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Scheduled(fixedRate = 60000)
    public void checkProductStatus() {

        for (var productConfig : properties.getProducts()) {
            checkSingleProduct(productConfig);
        }
    }

    private void checkSingleProduct(ProductTrackerProperties.ProductConfig config) {
        try {
            log.info("🔁 Checking product page... {}", config.getName());

            Document doc = Jsoup.connect(config.getUrl()).get();
            Element button = doc.selectFirst(".cart-submit-button");

            if (button == null) {
                log.warn("Button not found for {}", config.getName());
                return;
            }

            String buttonText = button.text().trim();

            ProductEntity product = productRepository
                    .findByUrl(config.getUrl())
                    .orElseGet(() -> {
                        ProductEntity p = new ProductEntity();
                        p.setName(config.getName());
                        p.setUrl(config.getUrl());
                        return p;
                    });

            String previousText = product.getButtonText();
            boolean isNew = product.getId() == null;

            if (previousText == null || !previousText.equalsIgnoreCase(buttonText)) {

                product.updateFromButtonText(buttonText);
                productRepository.save(product);

                if (!isNew) {
                    eventPublisher.publishEvent(
                            new ProductStatusChangedEvent(product)
                    );
                }
            }

        } catch (Exception e) {
            log.error("Error checking {}: {}", config.getName(), e.getMessage(), e);
        }
    }
}