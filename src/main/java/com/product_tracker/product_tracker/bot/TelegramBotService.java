package com.product_tracker.product_tracker.bot;

import com.product_tracker.product_tracker.config.ProductTrackerProperties;
import com.product_tracker.product_tracker.domain.AvailabilityStatus;
import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.entity.SubscriberEntity;
import com.product_tracker.product_tracker.service.ProductService;
import com.product_tracker.product_tracker.service.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {
    private final ProductService productService;
    private final ProductTrackerProperties properties;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUserName;

    private final SubscriberService subscriberService;
    private final AtomicReference<String> lastStatus =
            new AtomicReference<>("Not checked yet");

    public TelegramBotService(
            SubscriberService subscriberService,
            ProductService productService,
            ProductTrackerProperties properties
    ) {
        this.subscriberService = subscriberService;
        this.productService = productService;
        this.properties = properties;
    }

    @Override
    public String getBotToken() { return botToken; }

    @Override
    public String getBotUsername() { return botUserName; }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText().trim().toLowerCase();

            switch (text) {
                case "/start", "start" -> subscribe(chatId);
                case "/stop", "stop" -> unsubscribe(chatId);
                case "/test", "test" -> sendMessage(chatId,
                        "App running.\nCurrent status: " + lastStatus.get());
                default -> sendMessage(chatId, "Use /start to subscribe and /stop to unsubscribe");
            }
        }
    }

    public void updateLastButtonText(String status) {
        lastStatus.set(status);
    }

    private void subscribe(String chatId) {
        if (subscriberService.subscribe(chatId)) {
            sendMessage(chatId, "Subscribed to product alerts.\nFetching current status...");

            // send current status for all products
            for (var config : properties.getProducts()) {
                ProductEntity product =
                        productService.getOrCreateProduct(config.getName(), config.getUrl());

                String status = product.getAvailabilityStatus() == AvailabilityStatus.IN_STOCK
                        ? "✅ Available"
                        : "❌ Out of Stock";

                sendMessage(chatId,
                        config.getName() + "\nStatus: " + status);
            }

        } else {
            sendMessage(chatId, "Already subscribed.");
        }
    }

    private void unsubscribe(String chatId) {
        if (subscriberService.unsubscribe(chatId)) {
            sendMessage(chatId, "Unsubscribed.");
        } else {
            sendMessage(chatId, "Not subscribed.");
        }
    }

    public void broadcastMessage(String message) {
        List<SubscriberEntity> subs = subscriberService.getAllSubscribers();
        for (SubscriberEntity sub : subs) {
            sendMessage(sub.getChatId(), message);
        }
    }

    private void sendMessage(String chatId, String text) {
        try {
            execute(new SendMessage(chatId, text));
        } catch (Exception e) {
            log.error("Telegram error: {}", e.getMessage());
        }
    }
}