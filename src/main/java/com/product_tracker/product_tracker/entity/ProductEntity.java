package com.product_tracker.product_tracker.entity;

import com.product_tracker.product_tracker.domain.AvailabilityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String url;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    private LocalDateTime lastChecked;

    /** When the product was last seen in stock (for admin reporting). */
    private LocalDateTime lastInStockAt;

    public void updateAvailability(boolean available) {
        this.availabilityStatus =
                available ? AvailabilityStatus.IN_STOCK : AvailabilityStatus.OUT_OF_STOCK;
        this.lastChecked = LocalDateTime.now();
        if (available) {
            this.lastInStockAt = LocalDateTime.now();
        }
    }
}