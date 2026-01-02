package com.auntieescafe.auntieesfoodordermanagement.service.impl;

import com.auntieescafe.auntieesfoodordermanagement.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("Attempting to send OTP email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("saipreetham0929@gmail.com"); // Your configured email from application.properties
            message.setTo(toEmail);
            message.setSubject("Auntie's Kitchen: Your OTP for Email Verification");
            message.setText("Dear User,\n\nYour One-Time Password (OTP) for Auntie's Kitchen email verification is: " + otp + "\n\nThis OTP is valid for 5 minutes.\n\nIf you did not request this, please ignore this email.\n\nRegards,\nAuntie's Kitchen Team");

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            // Depending on requirements, you might want to rethrow a custom exception or handle it differently
            throw new RuntimeException("Failed to send OTP email.", e);
        }
    }
}
