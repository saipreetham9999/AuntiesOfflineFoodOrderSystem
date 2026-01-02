package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.entity.VerificationToken;

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
    // Removed role management methods as role is now a String in User entity
    // void addRoleToUser(UUID userId, String roleName);
    // void removeRoleFromUser(UUID userId, String roleName);

    // Methods for email verification (OTP-based)
    String generateAndSaveOtp(User user); // Generates OTP, saves token, returns OTP
    boolean verifyOtp(String email, String otp); // Verifies OTP for a given email
}
