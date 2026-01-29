package com.product_tracker.product_tracker.bot;

import com.product_tracker.product_tracker.entity.SubscriberEntity;
import com.product_tracker.product_tracker.service.ProductCheckerService;
import com.product_tracker.product_tracker.service.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUserName;

    private final SubscriberService subscriberService;
    private final AtomicReference<String> lastButtonText = new AtomicReference<>("Button text not yet fetched");

    public TelegramBotService(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText().trim().toLowerCase();

            switch (text) {
                case "/start", "start" -> handleStartCommand(chatId);
                case "/stop", "stop" -> handleStopCommand(chatId);
                case "/test", "test" -> handleTestCommand(chatId);
                default -> handleOtherMessages(chatId);
            }
        }
    }

    private void handleStartCommand(String chatId) {
        boolean subscribed = subscriberService.subscribe(chatId);
        if (subscribed) {
            sendMessage(chatId, """
                    You’ve successfully subscribed to product alerts!

                    I’ll notify you whenever the product status changes.
                    Use /stop anytime to unsubscribe.
                    """);
            log.info("New subscriber added: {}", chatId);
        } else {
            sendMessage(chatId, "You’re already subscribed to product alerts!");
            log.info("User {} tried to subscribe again.", chatId);
        }
    }

    private void handleStopCommand(String chatId) {
        boolean unsubscribed = subscriberService.unsubscribe(chatId);
        if (unsubscribed) {
            sendMessage(chatId, "You’ve been unsubscribed from product alerts. You won’t receive updates anymore.");
            log.info("Subscriber removed: {}", chatId);
        } else {
            sendMessage(chatId, "You’re not subscribed yet. Use /start to subscribe.");
            log.info("Unsubscribe request from non-subscriber: {}", chatId);
        }
    }
    private void handleTestCommand(String chatId) {
        String buttonText = lastButtonText.get(); // You’ll define this
        sendMessage(chatId, " App is running!\nCurrent button text: " + buttonText);
        return;
    }

    private void handleOtherMessages(String chatId) {
        sendMessage(chatId, """
                Hi there! I’m your Product Tracker Bot.

                I’ll help you stay updated when your desired product becomes available.
                Use /start to subscribe to alerts, or /stop to unsubscribe anytime.
                """);
        log.info("Sent welcome message to user: {}", chatId);
    }

    public void sendMessage(String chatId, String message) {
        SendMessage msg = new SendMessage(chatId, message);
        try {
            execute(msg);
        } catch (Exception e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage());
        }
    }

    public void broadcastMessage(String message) {
        List<SubscriberEntity> subscribers = subscriberService.getAllSubscribers();
        for (SubscriberEntity sub : subscribers) {
            sendMessage(sub.getChatId(), message);
        }
        log.info("Broadcasted message to {} subscribers.", subscribers.size());
    }
    public void updateLastButtonText(String buttonText) {
        lastButtonText.set(buttonText);
    }

}
