package com.product_tracker.product_tracker.entity;

import com.product_tracker.product_tracker.domain.AvailabilityStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String url;
    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;
    private String buttonText;

    private LocalDateTime lastUpdated;
    public void updateFromButtonText(String buttonText) {
        this.buttonText = buttonText;
        this.availabilityStatus =
                buttonText.equalsIgnoreCase("Add to cart")
                        ? AvailabilityStatus.AVAILABLE
                        : AvailabilityStatus.NOT_AVAILABLE;
        this.lastUpdated = LocalDateTime.now();
    }
}

