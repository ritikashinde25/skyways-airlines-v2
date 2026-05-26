package com.skyways.service;

import com.skyways.config.EncryptionConfig;
import com.skyways.constants.AppConstants;
import com.skyways.dto.AuthResponseDTO;
import com.skyways.dto.LoginDTO;
import com.skyways.dto.UserDTO;
import com.skyways.entity.User;
import com.skyways.exception.DuplicateResourceException;
import com.skyways.exception.InvalidCredentialsException;
import com.skyways.exception.ResourceNotFoundException;
import com.skyways.repository.UserRepository;
import com.skyways.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger =
        LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EncryptionConfig encryptionConfig;
    private final NotificationService notificationService;

    public String registerUser(UserDTO userDTO) {
        logger.info("Registering new user: {}",
            userDTO.getUsername());

        Optional.ofNullable(userDTO.getUsername())
            .filter(u -> !u.isBlank())
            .orElseThrow(() -> new IllegalArgumentException(
                AppConstants.ERROR_FIELDS_REQUIRED));

        if (userRepository.existsByUsername(
                userDTO.getUsername())) {
            logger.warn("Username already exists: {}",
                userDTO.getUsername());
            throw new DuplicateResourceException(
                AppConstants.ERROR_DUPLICATE_USERNAME);
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            logger.warn("Email already exists: {}",
                userDTO.getEmail());
            throw new DuplicateResourceException(
                AppConstants.ERROR_DUPLICATE_EMAIL);
        }

        String encryptedPassword = encryptionConfig.encrypt(
            userDTO.getPassword());
        logger.info("Password encrypted for user: {}",
            userDTO.getUsername());

        User user = User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(encryptedPassword)
                .role("USER")
                .build();

        userRepository.save(user);
        logger.info("User registered successfully: {}",
            userDTO.getUsername());
        return AppConstants.SUCCESS_REGISTER + ": " +
            userDTO.getUsername();
    }

    public AuthResponseDTO loginUser(LoginDTO loginDTO) {
        logger.info("Login attempt for user: {}",
            loginDTO.getUsername());

        User user = userRepository
            .findByUsername(loginDTO.getUsername())
            .orElseThrow(() -> {
                logger.warn("User not found: {}",
                    loginDTO.getUsername());
                return new ResourceNotFoundException(
                    AppConstants.ERROR_USER_NOT_FOUND);
            });

        String decryptedPassword = encryptionConfig.decrypt(
            user.getPassword());

        if (!decryptedPassword.equals(loginDTO.getPassword())) {
            logger.warn("Invalid password for user: {}",
                loginDTO.getUsername());
            throw new InvalidCredentialsException(
                AppConstants.ERROR_INVALID_PASSWORD);
        }

        String token = jwtUtil.generateToken(user.getUsername());
        logger.info("Login successful for user: {}",
            loginDTO.getUsername());

        return AuthResponseDTO.builder()
                .message(AppConstants.SUCCESS_LOGIN)
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public String forgotPassword(String email) {
        logger.info("Forgot password request for: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.warn("Email not found: {}", email);
                return new ResourceNotFoundException(
                    "No account found with email: " + email);
            });

        String resetToken = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() +
            (30 * 60 * 1000); // 30 minutes

        user.setResetToken(resetToken);
        user.setResetTokenExpiry(expiry);
        userRepository.save(user);

        String resetLink = 
            "http://localhost:4200/reset-password?token=" +
            resetToken;

        try {
            notificationService.sendPasswordResetEmail(
                user.getUsername(),
                email,
                resetLink);
            logger.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            logger.warn("Reset email failed: {}", e.getMessage());
        }

        return "Password reset link sent to your email!";
    }

    public String resetPassword(String token, String newPassword) {
        logger.info("Reset password request for token: {}", token);

        User user = userRepository.findByResetToken(token)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Invalid or expired reset token!"));

        if (System.currentTimeMillis() > 
                user.getResetTokenExpiry()) {
            throw new InvalidCredentialsException(
                "Reset token has expired! " +
                "Please request a new one.");
        }

        String encryptedPassword = encryptionConfig.encrypt(
            newPassword);
        user.setPassword(encryptedPassword);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        logger.info("Password reset successful for: {}",
            user.getUsername());
        return "Password reset successfully! Please login.";
    }

    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }
}