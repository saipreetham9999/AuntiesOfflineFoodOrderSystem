package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.config.JwtService;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.payload.LoginRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.OtpVerificationRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.RegisterRequest;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private EmailService emailService;
    private JwtService jwtService;
    private UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for email: {}", registerRequest.getEmail());

        if (userService.getUserByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} is already taken.", registerRequest.getEmail());
            return new ResponseEntity<>(Map.of("message", "Email is already taken!"), HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole("CUSTOMER");

        User registeredUser = userService.createUser(user);
        log.info("User {} registered successfully. Generating OTP...", registeredUser.getEmail());

        String otp = userService.generateAndSaveOtp(registeredUser);
        emailService.sendOtpEmail(registeredUser.getEmail(), otp);
        log.info("OTP sent to {} for verification.", registeredUser.getEmail());

        return new ResponseEntity<>(Map.of("message", "User registered successfully. Please check your email for OTP verification."), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        log.info("Received login request for email: {}", loginRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate JWT Token
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            String jwtToken = jwtService.generateToken(userDetails);
            
            // Get Role
            String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();
            
            log.info("User {} logged-in successfully.", loginRequest.getEmail());
            return new ResponseEntity<>(Map.of(
                    "message", "User logged-in successfully!",
                    "token", jwtToken,
                    "role", role
            ), HttpStatus.OK);
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
            userService.markEmailAsVerified(otpVerificationRequest.getEmail());
            return new ResponseEntity<>(Map.of("message", "OTP verified successfully. Account activated!"), HttpStatus.OK);
        } else {
            log.warn("OTP verification failed for email: {}", otpVerificationRequest.getEmail());
            return new ResponseEntity<>(Map.of("message", "Invalid or expired OTP."), HttpStatus.BAD_REQUEST);
        }
    }
}
