package com.auntieescafe.auntieesfoodordermanagement.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
    void sendUserCreationNotification(String adminEmail, String newUserName, String newUserEmail, String newUserRole);
    void sendUserUpdateNotification(String adminEmail, String updatedUserName, String updatedUserEmail, String updatedUserRole);
    void sendOrderCompletionEmail(String toEmail, String customerName, String orderId);
}
