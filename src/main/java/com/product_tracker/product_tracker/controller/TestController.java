package com.product_tracker.product_tracker.controller;

import com.product_tracker.product_tracker.bot.TelegramBotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "Product Tracker Bot is running successfully!";
    }


}