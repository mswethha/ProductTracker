package com.product_tracker.product_tracker.repository;
import com.product_tracker.product_tracker.entity.SubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<SubscriberEntity, Long> {
    Optional<SubscriberEntity> findByChatId(String chatId);
    void deleteByChatId(String chatId);
}


