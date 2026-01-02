package com.auntieescafe.auntieesfoodordermanagement.service.impl;

import com.auntieescafe.auntieesfoodordermanagement.entity.Role;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.entity.VerificationToken;
import com.auntieescafe.auntieesfoodordermanagement.repository.RoleRepository;
import com.auntieescafe.auntieesfoodordermanagement.repository.UserRepository;
import com.auntieescafe.auntieesfoodordermanagement.repository.VerificationTokenRepository;
import com.auntieescafe.auntieesfoodordermanagement.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import @Slf4j
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j // ADDED: Lombok annotation for logging
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private VerificationTokenRepository verificationTokenRepository;

    @Override
    public User createUser(User user) {
        log.info("Attempting to create new user with email: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Override
    public Optional<User> getUserById(UUID userId) {
        log.debug("Attempting to retrieve user by ID: {}", userId);
        Optional<User> user = userRepository.findById(userId);
        user.ifPresentOrElse(
                u -> log.debug("User found by ID: {}", userId),
                () -> log.debug("User not found by ID: {}", userId)
        );
        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        log.debug("Attempting to retrieve user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        user.ifPresentOrElse(
                u -> log.debug("User found by email: {}", email),
                () -> log.debug("User not found by email: {}", email)
        );
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Retrieving all users.");
        return userRepository.findAll();
    }

    @Override
    public User updateUser(UUID userId, User updatedUser) {
        log.info("Attempting to update user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for update with ID: {}", userId);
                    return new RuntimeException("User not found with id: " + userId);
                });
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            log.debug("User password updated for ID: {}", userId);
        }
        user.setEmailVerified(updatedUser.isEmailVerified());
        User savedUser = userRepository.save(user);
        log.info("User with ID {} updated successfully.", userId);
        return savedUser;
    }

    @Override
    public void deleteUser(UUID userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        userRepository.deleteById(userId);
        log.info("User with ID {} deleted successfully.", userId);
    }

    @Override
    public void addRoleToUser(UUID userId, String roleName) {
        log.info("Attempting to add role '{}' to user with ID: {}", roleName, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found to add role with ID: {}", userId);
                    return new RuntimeException("User not found with id: " + userId);
                });
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Role '{}' not found to add to user with ID: {}", roleName, userId);
                    return new RuntimeException("Role not found with name: " + roleName);
                });
        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Role '{}' added to user with ID {} successfully.", roleName, userId);
    }

    @Override
    public void removeRoleFromUser(UUID userId, String roleName) {
        log.info("Attempting to remove role '{}' from user with ID: {}", roleName, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found to remove role with ID: {}", userId);
                    return new RuntimeException("User not found with id: " + userId);
                });
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Role '{}' not found to remove from user with ID: {}", roleName, userId);
                    return new RuntimeException("Role not found with name: " + roleName);
                });
        user.getRoles().remove(role);
        userRepository.save(user);
        log.info("Role '{}' removed from user with ID {} successfully.", roleName, userId);
    }

    @Override
    @Transactional
    public String generateAndSaveOtp(User user) {
        log.info("Generating and saving OTP for user with email: {}", user.getEmail());
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000); // Generate a 6-digit OTP
        String otp = String.valueOf(otpValue);
        log.debug("Generated OTP: {}", otp);

        // Invalidate any existing OTP for this user to ensure only one is active
        verificationTokenRepository.findByUser(user).ifPresent(token -> {
            verificationTokenRepository.delete(token);
            log.debug("Deleted any existing OTP for user: {}", user.getEmail());
        });


        VerificationToken myToken = new VerificationToken(user, otp);
        verificationTokenRepository.save(myToken);
        log.info("OTP for user {} saved successfully. Expiry: {}", user.getEmail(), myToken.getExpiryDate());
        return otp;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        log.info("Attempting to verify OTP for email: {} with OTP: {}", email, otp);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.warn("User not found for email: {}. OTP verification failed.", email);
            return false;
        }
        User user = userOptional.get();
        log.debug("User found for email: {}", email);

        // Find by user and token, as a user might have multiple tokens if not invalidated properly
        Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByUserAndToken(user, otp);
        if (verificationTokenOptional.isEmpty()) {
            log.warn("Verification token not found for user {} with OTP: {}. OTP verification failed.", user.getEmail(), otp);
            return false;
        }

        VerificationToken verificationToken = verificationTokenOptional.get();

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("OTP for user {} has expired. Expiry: {}. OTP verification failed.", user.getEmail(), verificationToken.getExpiryDate());
            verificationTokenRepository.delete(verificationToken); // Delete the expired token
            return false;
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("User {} successfully verified via OTP. Email verified status set to true.", user.getEmail());

        verificationTokenRepository.delete(verificationToken); // Delete the token after successful verification
        log.debug("Deleted used verification token for user {}.", user.getEmail());
        return true;
    }
}
