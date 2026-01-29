package com.product_tracker.product_tracker.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
@ConfigurationProperties(prefix = "product.tracker")
@Getter
@Setter
public class ProductTrackerProperties {
    private List<ProductConfig> products;

    @Getter
    @Setter
    public static class ProductConfig {
        private String name;
        private String url;
    }
}
