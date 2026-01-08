package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.payload.LoginRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.OtpVerificationRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.RegisterRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.LoginResponse;
import com.auntieescafe.auntieesfoodordermanagement.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for email: {}", registerRequest.getEmail());
        try {
            authService.registerUser(registerRequest);
            return new ResponseEntity<>(Map.of("message", "User registered successfully. Please check your email for OTP verification."), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.getEmail());
        try {
            LoginResponse loginResponse = authService.loginUser(loginRequest);
            log.info("User {} logged-in successfully.", loginRequest.getEmail());
            return ResponseEntity.ok(loginResponse);
        } catch (AuthenticationException e) {
            log.warn("Login failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return new ResponseEntity<>(Map.of("message", "Invalid credentials or account not verified."), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest otpVerificationRequest) {
        log.info("Received OTP verification request for email: {}", otpVerificationRequest.getEmail());
        try {
            authService.verifyOtpAndActivateUser(otpVerificationRequest.getEmail(), otpVerificationRequest.getOtp());
            return ResponseEntity.ok(Map.of("message", "OTP verified successfully. Account activated!"));
        } catch (IllegalArgumentException e) {
            log.warn("OTP verification failed for email {}: {}", otpVerificationRequest.getEmail(), e.getMessage());
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
