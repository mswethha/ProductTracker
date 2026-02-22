package com.product_tracker.product_tracker.controller;

import com.product_tracker.product_tracker.config.ProductTrackerProperties;
import com.product_tracker.product_tracker.entity.UserEntity;
import com.product_tracker.product_tracker.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final ProductTrackerProperties properties;

    /** Current user info (for frontend: who is logged in, is admin?). */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        MeResponse r = new MeResponse();
        r.setUsername(user.getUsername());
        r.setAdmin(user.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
        return ResponseEntity.ok(r);
    }

    /** Form data: POST /api/auth/register with Content-Type: application/x-www-form-urlencoded */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {
        return doRegister(username, email, password);
    }

    /** JSON body: POST /api/auth/register/json with Content-Type: application/json */
    @PostMapping(value = "/register/json", consumes = "application/json")
    public ResponseEntity<?> registerUserJson(@Valid @RequestBody RegisterRequest request) {
        return doRegister(request.getUsername(), request.getEmail(), request.getPassword());
    }

    /** Create first admin: POST /api/auth/register-admin with JSON body including "secret" (must match product.tracker.admin-secret). */
    @PostMapping(value = "/register-admin", consumes = "application/json")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterAdminRequest request) {
        if (properties.getAdminSecret() == null || properties.getAdminSecret().isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Admin registration not configured"));
        }
        if (!properties.getAdminSecret().equals(request.getSecret())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("Invalid secret"));
        }
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Username already exists"));
        }
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Email already exists"));
        }
        UserEntity admin = userService.createAdmin(request.getUsername(), request.getEmail(), request.getPassword());
        RegisterResponse r = new RegisterResponse();
        r.setId(admin.getId());
        r.setUsername(admin.getUsername());
        r.setEmail(admin.getEmail());
        r.setMessage("Admin created. Use this account to log in to the Admin panel.");
        return ResponseEntity.status(HttpStatus.CREATED).body(r);
    }
    /** POST /api/auth/link-telegram  body: { "chatId": "123456" } */
    @PostMapping("/link-telegram")
    public ResponseEntity<?> linkTelegram(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        if (userDetails == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String chatId = body.get("chatId");
        if (chatId == null || chatId.isBlank())
            return ResponseEntity.badRequest().body(new ErrorResponse("chatId required"));
        try {
            userService.linkTelegram(userDetails.getUsername(), chatId);
            return ResponseEntity.ok(Map.of("message", "Telegram linked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Failed to link: " + e.getMessage()));
        }
    }
    private ResponseEntity<?> doRegister(String username, String email, String password) {
        try {
            if (username == null || username.isBlank() || username.length() < 3) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Username must be at least 3 characters"));
            }
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email is required"));
            }
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Password must be at least 6 characters"));
            }

            if (userService.findByUsername(username).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Username already exists"));
            }
            if (userService.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Email already exists"));
            }

            UserEntity user = userService.registerUser(username, email, password);

            RegisterResponse response = new RegisterResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setMessage("User registered successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to register user: " + e.getMessage()));
        }
    }

    @Data
    static class RegisterAdminRequest {
        private String secret;
        private String username;
        private String email;
        private String password;
    }

    @Data
    static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    @Data
    static class RegisterResponse {
        private Long id;
        private String username;
        private String email;
        private String message;
    }

    @Data
    static class MeResponse {
        private String username;
        private boolean admin;
    }

    @Data
    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
