package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.CreateUserRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(UUID userId);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    User updateUser(UUID userId, User user);
    void deleteUser(UUID userId);

    // Methods for email verification (OTP-based)
    String generateAndSaveOtp(User user);
    boolean verifyOtp(String email, String otp);
    void markEmailAsVerified(String email);

    // Admin specific methods
    User adminCreateUser(CreateUserRequest request);
    User updateUserRole(UUID userId, String newRole);

    // Secure Admin Promotion
    String initiateAdminPromotion(UUID userId);
    User confirmAdminPromotion(String otp);
}
