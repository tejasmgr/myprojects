package com.sunbeam.service.impl;

import com.sunbeam.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
//import javax.mail.MessagingException;
//import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        Context context = new Context();
        context.setVariable("verificationLink", "http://localhost:3000/verify-email?token=" + token);
        context.setVariable("token", token); // Setting the token separately for manual way
        String htmlContent = templateEngine.process("email/verification", context);
        sendHtmlEmail(toEmail, "Verify Your Email", htmlContent);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        Context context = new Context();
        context.setVariable("resetLink", 
            "http://localhost:3000/reset-password?token=" + token);
        context.setVariable("resetToken", 
                ""+ token);

        String htmlContent = templateEngine.process("email/password-reset", context);

        sendHtmlEmail(toEmail, "Password Reset Request", htmlContent);
    }

    @Override
    public void sendApplicationStatusUpdate(String toEmail, String subject, String body) {
        Context context = new Context();
        context.setVariable("message", body);

        String htmlContent = templateEngine.process("email/status-update", context);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Override
    public void sendSystemAlert(String toEmail, String subject, String message) {
        sendHtmlEmail(toEmail, subject, 
            "<h3>System Alert</h3><p>" + message + "</p>");
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@portal.gov.in");
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}