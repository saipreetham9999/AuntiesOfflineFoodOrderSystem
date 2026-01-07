package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.Token;
import com.auntieescafe.auntieesfoodordermanagement.entity.TokenType;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.CreateUserRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.TokenRepository;
import com.auntieescafe.auntieesfoodordermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${application.security.admin-promotion.super-admin-email}")
    private String superAdminEmail;

    private String sanitizeRole(String role) {
        if (role != null && role.toUpperCase().startsWith("ROLE_")) {
            return role.substring(5).toUpperCase();
        }
        return role != null ? role.toUpperCase() : null;
    }

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("Email is already in use.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(sanitizeRole(user.getRole()));
        return userRepository.save(user);
    }

    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public String generateAndSaveOtp(User user) {
        String otp = generateOtp();
        tokenRepository.findByUserAndType(user, TokenType.EMAIL_VERIFICATION)
                .ifPresent(tokenRepository::delete);
        Token newToken = new Token(user, otp, TokenType.EMAIL_VERIFICATION);
        tokenRepository.save(newToken);
        return otp;
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        return userRepository.findByEmail(email)
                .flatMap(user -> tokenRepository.findByTokenAndType(otp, TokenType.EMAIL_VERIFICATION))
                .map(token -> {
                    if (token.isExpired() || !token.getUser().getEmail().equals(email)) {
                        tokenRepository.delete(token);
                        return false;
                    }
                    tokenRepository.delete(token);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public void markEmailAsVerified(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                userRepository.save(user);
            }
        });
    }

    public User adminCreateUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email is already in use.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(sanitizeRole(request.getRole()));
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if ("KITCHEN".equals(user.getRole()) || "CASHIER".equals(user.getRole())) {
            emailService.sendUserCreationNotification(superAdminEmail, user.getName(), user.getEmail(), user.getRole());
        }
        
        return userRepository.save(user);
    }

    public User updateUserRole(UUID userId, String newRole) {
        if (!StringUtils.hasText(newRole)) {
            throw new IllegalArgumentException("Role cannot be empty.");
        }

        String sanitizedNewRole = sanitizeRole(newRole);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if ("ADMIN".equals(user.getRole()) && !"ADMIN".equals(sanitizedNewRole)) {
            throw new SecurityException("Admins cannot be demoted.");
        }

        if ("ADMIN".equals(sanitizedNewRole)) {
            throw new SecurityException("Admin promotion requires the secure OTP process. Please use the /api/admin/promote endpoints.");
        }

        user.setRole(sanitizedNewRole);
        
        if ("KITCHEN".equals(user.getRole()) || "CASHIER".equals(user.getRole())) {
            emailService.sendUserUpdateNotification(superAdminEmail, user.getName(), user.getEmail(), user.getRole());
        }

        return userRepository.save(user);
    }

    @Transactional
    public String initiateAdminPromotion(UUID userId) {
        User userToPromote = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if ("ADMIN".equals(userToPromote.getRole())) {
            throw new IllegalStateException("User is already an admin.");
        }

        String otp = generateOtp();
        tokenRepository.deleteByUserAndType(userToPromote, TokenType.ADMIN_PROMOTION);

        Token promotionToken = new Token(userToPromote, otp, TokenType.ADMIN_PROMOTION);
        tokenRepository.save(promotionToken);

        log.info("Sending admin promotion OTP to super admin: {}", superAdminEmail);
        emailService.sendOtpEmail(superAdminEmail, otp);

        return "OTP sent to the super admin's email for verification.";
    }

    @Transactional
    public User confirmAdminPromotion(String otp) {
        Token promotionToken = tokenRepository.findByTokenAndType(otp, TokenType.ADMIN_PROMOTION)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP."));

        if (promotionToken.isExpired()) {
            tokenRepository.delete(promotionToken);
            throw new RuntimeException("OTP has expired.");
        }

        User userToPromote = promotionToken.getUser();
        userToPromote.setRole("ADMIN");
        userRepository.save(userToPromote);

        tokenRepository.delete(promotionToken);

        log.info("User {} has been successfully promoted to ADMIN.", userToPromote.getEmail());
        return userToPromote;
    }

    public List<User> searchCustomers(String query) {
        return userRepository.searchCustomers(query);
    }

    private String generateOtp() {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        return String.valueOf(otpValue);
    }
}
