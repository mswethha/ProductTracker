package com.product_tracker.product_tracker.bot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String botUserName;
    @Value("${telegram.chat.id}")
    private  String chatId;
    @Override
    public void onUpdateReceived(Update update) {

    }
    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }
    public void sendMessage(String message) {
        SendMessage msg = new SendMessage(chatId, message);
        try { execute(msg); } catch (Exception e) { e.printStackTrace(); }
    }
}

