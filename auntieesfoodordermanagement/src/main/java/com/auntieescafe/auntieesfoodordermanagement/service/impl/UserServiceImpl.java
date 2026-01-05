package com.auntieescafe.auntieesfoodordermanagement.service.impl;

import com.auntieescafe.auntieesfoodordermanagement.entity.AdminPromotionToken;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.entity.VerificationToken;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.CreateUserRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.AdminPromotionTokenRepository;
import com.auntieescafe.auntieesfoodordermanagement.repository.UserRepository;
import com.auntieescafe.auntieesfoodordermanagement.repository.VerificationTokenRepository;
import com.auntieescafe.auntieesfoodordermanagement.service.EmailService;
import com.auntieescafe.auntieesfoodordermanagement.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AdminPromotionTokenRepository adminPromotionTokenRepository;
    private final EmailService emailService;

    @Value("${application.security.admin-promotion.super-admin-email}")
    private String superAdminEmail;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, VerificationTokenRepository verificationTokenRepository, AdminPromotionTokenRepository adminPromotionTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.adminPromotionTokenRepository = adminPromotionTokenRepository;
        this.emailService = emailService;
    }

    private String sanitizeRole(String role) {
        if (role != null && role.toUpperCase().startsWith("ROLE_")) {
            return role.substring(5).toUpperCase();
        }
        return role != null ? role.toUpperCase() : null;
    }

    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(sanitizeRole(user.getRole()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(UUID userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        user.setEmailVerified(updatedUser.isEmailVerified());
        user.setRole(sanitizeRole(updatedUser.getRole()));
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public String generateAndSaveOtp(User user) {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        verificationTokenRepository.findByUser(user).ifPresent(verificationTokenRepository::delete);
        VerificationToken myToken = new VerificationToken(user, otp);
        verificationTokenRepository.save(myToken);
        return otp;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        return userRepository.findByEmail(email)
                .flatMap(user -> verificationTokenRepository.findByUserAndToken(user, otp))
                .map(token -> {
                    if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                        verificationTokenRepository.delete(token);
                        return false;
                    }
                    verificationTokenRepository.delete(token);
                    return true;
                }).orElse(false);
    }

    @Override
    @Transactional
    public void markEmailAsVerified(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                userRepository.save(user);
            }
        });
    }

    @Override
    public User adminCreateUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(sanitizeRole(request.getRole()));
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User updateUserRole(UUID userId, String newRole) {
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
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public String initiateAdminPromotion(UUID userId) {
        User userToPromote = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if ("ADMIN".equals(userToPromote.getRole())) {
            throw new IllegalStateException("User is already an admin.");
        }

        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);

        adminPromotionTokenRepository.findByUser(userToPromote).ifPresent(adminPromotionTokenRepository::delete);

        AdminPromotionToken promotionToken = new AdminPromotionToken(userToPromote, otp);
        adminPromotionTokenRepository.save(promotionToken);

        log.info("Sending admin promotion OTP to super admin: {}", superAdminEmail);
        emailService.sendOtpEmail(superAdminEmail, otp);

        return "OTP sent to the super admin's email for verification.";
    }

    @Override
    @Transactional
    public User confirmAdminPromotion(String otp) {
        AdminPromotionToken promotionToken = adminPromotionTokenRepository.findByToken(otp)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP."));

        if (promotionToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            adminPromotionTokenRepository.delete(promotionToken);
            throw new RuntimeException("OTP has expired.");
        }

        User userToPromote = promotionToken.getUser();
        userToPromote.setRole("ADMIN");
        userRepository.save(userToPromote);

        adminPromotionTokenRepository.delete(promotionToken);

        log.info("User {} has been successfully promoted to ADMIN.", userToPromote.getEmail());
        return userToPromote;
    }

    @Override
    public List<User> searchCustomers(String query) {
        return userRepository.searchCustomers(query);
    }
}
