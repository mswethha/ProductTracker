package com.product_tracker.product_tracker.service;

import com.product_tracker.product_tracker.domain.UserRole;
import com.product_tracker.product_tracker.entity.UserEntity;
import com.product_tracker.product_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEntity registerUser(String username, String email, String password) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public UserEntity createAdmin(String username, String email, String password) {
        UserEntity admin = new UserEntity();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        return userRepository.save(admin);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UserEntity> findByTelegramChatId(String chatId) {
        return userRepository.findByTelegramChatId(chatId);
    }

    /** Generates a 6-digit code the user sends to the Telegram bot to link their account */
    public UserEntity generateLinkCode(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        user.setTelegramLinkCode(code);
        return userRepository.save(user);
    }

    /** Links a Telegram chatId directly to a user (kept for backwards compatibility) */
    public UserEntity linkTelegram(String username, String chatId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.setTelegramChatId(chatId);
        user.setTelegramLinkCode(null); // clear any existing code
        return userRepository.save(user);
    }

    /** Unlinks Telegram from a user */
    public void unlinkTelegram(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.setTelegramChatId(null);
        user.setTelegramLinkCode(null);
        userRepository.save(user);
    }
}