package com.product_tracker.product_tracker.event;

import com.product_tracker.product_tracker.entity.ProductEntity;

public record ProductStatusChangedEvent (ProductEntity product) {

}
