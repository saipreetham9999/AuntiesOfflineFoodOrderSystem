package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.config.JwtService;
import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.payload.LoginRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.RegisterRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        if (userService.getUserByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalStateException("Email is already taken!");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); // Password will be encoded by userService.createUser
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole("CUSTOMER");

        User registeredUser = userService.createUser(user);
        String otp = userService.generateAndSaveOtp(registeredUser);
        emailService.sendOtpEmail(registeredUser.getEmail(), otp);
    }

    public LoginResponse loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        return new LoginResponse("User logged-in successfully!", jwtToken, role);
    }

    @Transactional
    public void verifyOtpAndActivateUser(String email, String otp) {
        boolean isVerified = userService.verifyOtp(email, otp);
        if (!isVerified) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }
        userService.markEmailAsVerified(email);
    }
}
