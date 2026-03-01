package com.product_tracker.product_tracker.controller;

import com.product_tracker.product_tracker.bot.TelegramBotService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final PasswordEncoder passwordEncoder;
    private final TelegramBotService telegramBotService;
    @GetMapping("/")
    public String home() {
        return "Product Tracker Bot is running!";
    }
    @GetMapping("/api/test-alert")
    public String testAlert() {
        telegramBotService.broadcastMessage(
                "🧪 Test alert — stock notifications are working!"
        );
        return "Alert sent!";
    }
    // TEMPORARY — delete after use!
    @GetMapping("/api/hash")
    public String hashPassword(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
}