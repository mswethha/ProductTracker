package com.product_tracker.product_tracker.service;

import com.product_tracker.product_tracker.entity.SubscriberEntity;
import com.product_tracker.product_tracker.repository.SubscriberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;

    public SubscriberService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public boolean subscribe(String chatId) {
        if (subscriberRepository.findByChatId(chatId).isPresent()) {
            return false;
        }
        subscriberRepository.save(new SubscriberEntity(null, chatId));
        return true;
    }

    public boolean unsubscribe(String chatId) {
        Optional<SubscriberEntity> sub = subscriberRepository.findByChatId(chatId);
        if (sub.isPresent()) {
            subscriberRepository.delete(sub.get());
            return true;
        }
        return false;
    }

    public List<SubscriberEntity> getAllSubscribers() {
        return subscriberRepository.findAll();
    }
}