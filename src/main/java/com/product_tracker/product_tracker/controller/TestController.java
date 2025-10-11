package com.product_tracker.product_tracker.controller;

import com.product_tracker.product_tracker.bot.TelegramBotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final TelegramBotService telegramBotService;

    public TestController(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @GetMapping("/test-telegram")
    public String testTelegram() {
        telegramBotService.sendMessage("Hello! This is a test message from Spring Boot.");
        return "Message sent!";
    }
}