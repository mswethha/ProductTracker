package com.product_tracker.product_tracker.bot;
import com.product_tracker.product_tracker.repository.UserRepository;
import java.util.Optional;
import com.product_tracker.product_tracker.entity.UserEntity;
import com.product_tracker.product_tracker.config.ProductTrackerProperties;
import com.product_tracker.product_tracker.domain.AvailabilityStatus;
import com.product_tracker.product_tracker.entity.ProductEntity;
import com.product_tracker.product_tracker.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductTrackerProperties properties;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUserName;

    private final AtomicReference<String> lastStatus = new AtomicReference<>("Not checked yet");

    public TelegramBotService(
            UserRepository userRepository,
            ProductService productService,
            ProductTrackerProperties properties
    ) {
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
        String text = update.getMessage().getText().trim();

        if (text.equalsIgnoreCase("/start")) {
            sendMessage(chatId,
                    "👋 Welcome to Product Tracker!\n\n" +
                            "To receive alerts:\n" +
                            "1. Log in at http://localhost:5173\n" +
                            "2. Go to Profile → Generate Code\n" +
                            "3. Send the 6-digit code here"
            );
        } else if (text.equalsIgnoreCase("/stop")) {
            userRepository.findByTelegramChatId(chatId).ifPresentOrElse(
                    user -> {
                        user.setTelegramChatId(null);
                        userRepository.save(user);
                        sendMessage(chatId, " Unsubscribed successfully.");
                    },
                    () -> sendMessage(chatId, "You are not subscribed.")
            );
        } else if (text.matches("\\d{6}")) {
            userRepository.findByTelegramLinkCode(text).ifPresentOrElse(
                    user -> {
                        user.setTelegramChatId(chatId);
                        user.setTelegramLinkCode(null);
                        userRepository.save(user);
                        sendMessage(chatId,
                                " Linked! Hi " + user.getUsername() + "!\n" +
                                        "You'll now receive stock alerts here."
                        );
                    },
                    () -> sendMessage(chatId, " Invalid or expired code. Get a new one from the web app.")
            );
        } else if (text.equalsIgnoreCase("/status")) {
            userRepository.findByTelegramChatId(chatId).ifPresentOrElse(
                    user -> {
                        StringBuilder sb = new StringBuilder("📦 Product Status:\n\n");
                        for (var config : properties.getProducts()) {
                            ProductEntity p = productService.getOrCreateProduct(
                                    config.getName(), config.getUrl());
                            String status = p.getAvailabilityStatus() == AvailabilityStatus.IN_STOCK
                                    ? " In Stock" : " Out of Stock";
                            sb.append(config.getName()).append(": ").append(status).append("\n");
                        }
                        sendMessage(chatId, sb.toString());
                    },
                    () -> sendMessage(chatId, "Please link your account first. Use /start.")
            );
        } else {
            sendMessage(chatId, "Send your 6-digit code to link, or use /start for help.");
        }
    }

    public void broadcastMessage(String message) {
        userRepository.findAll().stream()
                .filter(u -> u.getTelegramChatId() != null
                        && !u.getTelegramChatId().isBlank())
                .forEach(u -> sendMessage(u.getTelegramChatId(), message));
    }

    public void updateLastButtonText(String status) {
        lastStatus.set(status);
    }

    private void sendMessage(String chatId, String text) {
        try { execute(new SendMessage(chatId, text)); }
        catch (Exception e) { log.error("Telegram error: {}", e.getMessage()); }
    }
}