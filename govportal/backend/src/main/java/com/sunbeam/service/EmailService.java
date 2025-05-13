package com.sunbeam.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String token);
    void sendPasswordResetEmail(String toEmail, String token);
    void sendApplicationStatusUpdate(String toEmail, String subject, String body);
    void sendSystemAlert(String toEmail, String subject, String message);
}