package com.auntieescafe.auntieesfoodordermanagement.service.impl;

import com.auntieescafe.auntieesfoodordermanagement.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.security.admin-promotion.super-admin-email}")
    private String superAdminEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("Attempting to send OTP email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Auntie's Kitchen: Your OTP for Email Verification");
            message.setText("Dear User,\n\nYour One-Time Password (OTP) for Auntie's Kitchen email verification is: " + otp + "\n\nThis OTP is valid for 5 minutes.\n\nIf you did not request this, please ignore this email.\n\nRegards,\nAuntie's Kitchen Team");

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email.", e);
        }
    }

    @Override
    public void sendUserCreationNotification(String adminEmail, String newUserName, String newUserEmail, String newUserRole) {
        log.info("Attempting to send user creation notification to admin: {}", adminEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("Auntie's Kitchen: New Staff Member Created");
            message.setText("A new staff member has been created:\n\n" +
                    "Name: " + newUserName + "\n" +
                    "Email: " + newUserEmail + "\n" +
                    "Role: " + newUserRole + "\n\n" +
                    "Regards,\nAuntie's Kitchen Team");

            mailSender.send(message);
            log.info("User creation notification sent successfully to: {}", adminEmail);
        } catch (MailException e) {
            log.error("Failed to send user creation notification to {}: {}", adminEmail, e.getMessage());
            throw new RuntimeException("Failed to send user creation notification.", e);
        }
    }

    @Override
    public void sendUserUpdateNotification(String adminEmail, String updatedUserName, String updatedUserEmail, String updatedUserRole) {
        log.info("Attempting to send user update notification to admin: {}", adminEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("Auntie's Kitchen: Staff Member Updated");
            message.setText("A staff member's details have been updated:\n\n" +
                    "Name: " + updatedUserName + "\n" +
                    "Email: " + updatedUserEmail + "\n" +
                    "Role: " + updatedUserRole + "\n\n" +
                    "Regards,\nAuntie's Kitchen Team");

            mailSender.send(message);
            log.info("User update notification sent successfully to: {}", adminEmail);
        } catch (MailException e) {
            log.error("Failed to send user update notification to {}: {}", adminEmail, e.getMessage());
            throw new RuntimeException("Failed to send user update notification.", e);
        }
    }

    @Override
    public void sendOrderCompletionEmail(String toEmail, String customerName, String orderId) {
        log.info("Attempting to send order completion email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Auntie's Kitchen Order is Ready!");
            message.setText("Hi " + customerName + ",\n\nYour order #" + orderId + " is now complete and ready for pickup. Thank you for your order!\n\nRegards,\nAuntie's Kitchen Team");

            mailSender.send(message);
            log.info("Order completion email sent successfully to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send order completion email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send order completion email.", e);
        }
    }
}
