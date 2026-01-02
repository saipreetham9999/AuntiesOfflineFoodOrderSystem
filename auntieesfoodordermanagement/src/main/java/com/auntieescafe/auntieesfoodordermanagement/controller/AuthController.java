package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.Role;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.payload.LoginRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.OtpVerificationRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.RegisterRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.RoleRepository;
import com.auntieescafe.auntieesfoodordermanagement.service.EmailService;
import com.auntieescafe.auntieesfoodordermanagement.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map; // Import Map

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private UserService userService;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for email: {}", registerRequest.getEmail());

        // Check if email already exists
        if (userService.getUserByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} is already taken.", registerRequest.getEmail());
            return new ResponseEntity<>(Map.of("message", "Email is already taken!"), HttpStatus.BAD_REQUEST);
        }

        // Create new user's account
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmailVerified(false); // Will be true after email verification
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Assign CUSTOMER role by default
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> {
                    log.error("Error: CUSTOMER role not found in database during registration.");
                    return new RuntimeException("Error: Role is not found.");
                });
        user.setRoles(Collections.singleton(customerRole));

        User registeredUser = userService.createUser(user);
        log.info("User {} registered successfully. Generating OTP...", registeredUser.getEmail());

        // Generate and send OTP
        String otp = userService.generateAndSaveOtp(registeredUser);
        emailService.sendOtpEmail(registeredUser.getEmail(), otp); // Call EmailService to send OTP
        log.info("OTP sent to {} for verification.", registeredUser.getEmail());

        return new ResponseEntity<>(Map.of("message", "User registered successfully. Please check your email for OTP verification."), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){ // Changed return type to ResponseEntity<?>
        log.info("Received login request for email: {}", loginRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("User {} logged-in successfully.", loginRequest.getEmail());
            return new ResponseEntity<>(Map.of("message", "User logged-in successfully!"), HttpStatus.OK);
        } catch (Exception e) {
            log.warn("Login failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return new ResponseEntity<>(Map.of("message", "Invalid credentials or account not verified."), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest otpVerificationRequest) {
        log.info("Received OTP verification request for email: {}", otpVerificationRequest.getEmail());
        boolean isVerified = userService.verifyOtp(otpVerificationRequest.getEmail(), otpVerificationRequest.getOtp());

        if (isVerified) {
            log.info("OTP verification successful for email: {}", otpVerificationRequest.getEmail());
            return new ResponseEntity<>(Map.of("message", "OTP verified successfully. Account activated!"), HttpStatus.OK);
        } else {
            log.warn("OTP verification failed for email: {}", otpVerificationRequest.getEmail());
            return new ResponseEntity<>(Map.of("message", "Invalid or expired OTP."), HttpStatus.BAD_REQUEST);
        }
    }
}
