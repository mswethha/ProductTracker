package com.product_tracker.product_tracker.bot;
import com.product_tracker.product_tracker.repository.UserRepository;
import java.util.Optional;
import com.product_tracker.product_tracker.entity.UserEntity;
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
    private final UserRepository userRepository;       // NEW
    private final ProductService productService;
    private final ProductTrackerProperties properties;
    private final SubscriberService subscriberService; // keep for backwards compat

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUserName;

    private final AtomicReference<String> lastStatus = new AtomicReference<>("Not checked yet");

    public TelegramBotService(
            SubscriberService subscriberService,
            UserRepository userRepository,
            ProductService productService,
            ProductTrackerProperties properties
    ) {
        this.subscriberService = subscriberService;
        this.userRepository = userRepository;
        this.productService = productService;
        this.properties = properties;
    }

    @Override public String getBotToken() { return botToken; }
    @Override public String getBotUsername() { return botUserName; }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;
        String chatId = update.getMessage().getChatId().toString();
        String text = update.getMessage().getText().trim().toLowerCase();

        switch (text) {
            case "/start", "start" -> handleStart(chatId);
            case "/stop",  "stop"  -> unsubscribe(chatId);
            case "/test",  "test"  -> sendMessage(chatId, "Running.\nStatus: " + lastStatus.get());
            default -> sendMessage(chatId,
                    "Use /start to subscribe, /stop to unsubscribe.\n" +
                            "Link your account at the web app first.");
        }
    }

    private void handleStart(String chatId) {
        // Check if this chatId is already linked to a user account
        Optional<UserEntity> linkedUser = userRepository.findByTelegramChatId(chatId);

        if (linkedUser.isPresent()) {
            sendMessage(chatId, "Welcome back, " + linkedUser.get().getUsername() + "! You are subscribed.");
        } else {
            // Fall back to anonymous subscriber
            subscriberService.subscribe(chatId);
            sendMessage(chatId,
                    "Subscribed! 📦\n\n" +
                            "💡 Tip: Link your web account at " +
                            "http://localhost:5173 → Profile → Link Telegram\n" +
                            "Your Telegram Chat ID: " + chatId);
        }

        // Send current status for all products
        for (var config : properties.getProducts()) {
            ProductEntity product = productService.getOrCreateProduct(config.getName(), config.getUrl());
            String status = product.getAvailabilityStatus() == AvailabilityStatus.IN_STOCK
                    ? "✅ Available" : "❌ Out of Stock";
            sendMessage(chatId, config.getName() + "\nStatus: " + status);
        }
    }

    private void unsubscribe(String chatId) {
        boolean removedAnon = subscriberService.unsubscribe(chatId);
        // Also unlink from user account if linked
        userRepository.findByTelegramChatId(chatId).ifPresent(user -> {
            user.setTelegramChatId(null);
            userRepository.save(user);
        });
        sendMessage(chatId, (removedAnon || true) ? "Unsubscribed." : "Not subscribed.");
    }

    public void broadcastMessage(String message) {
        // Broadcast to anonymous subscribers
        subscriberService.getAllSubscribers()
                .forEach(sub -> sendMessage(sub.getChatId(), message));

        // Broadcast to users who linked their Telegram
        userRepository.findAll().stream()
                .filter(u -> u.getTelegramChatId() != null && !u.getTelegramChatId().isBlank())
                .forEach(u -> sendMessage(u.getTelegramChatId(), message));
    }

    public void updateLastButtonText(String status) { lastStatus.set(status); }

    private void sendMessage(String chatId, String text) {
        try { execute(new SendMessage(chatId, text)); }
        catch (Exception e) { log.error("Telegram error: {}", e.getMessage()); }
    }
}