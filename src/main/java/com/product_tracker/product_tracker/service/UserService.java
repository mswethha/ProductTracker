package com.product_tracker.product_tracker.service;

import com.product_tracker.product_tracker.domain.UserRole;
import com.product_tracker.product_tracker.entity.UserEntity;
import com.product_tracker.product_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        return userRepository.save(user);
    }

    public UserEntity createAdmin(String username, String email, String password) {
        UserEntity admin = new UserEntity();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);

        return userRepository.save(admin);
    }
}