package com.product_tracker.product_tracker;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProductTrackerApplication {


    public static void main(String[] args) {
        SpringApplication.run(ProductTrackerApplication.class, args);
    }
}