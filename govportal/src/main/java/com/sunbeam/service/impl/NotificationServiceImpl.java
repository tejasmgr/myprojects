package com.sunbeam.service.impl;

import com.sunbeam.model.User;
import com.sunbeam.service.EmailService;
import com.sunbeam.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;

    @Override
    public void sendApplicationUpdate(User user, String applicationId, String status) {
        String subject = "Application Status Update";
        String body = String.format(
                "Your application %s is now %s",
                applicationId,
                status
        );
        emailService.sendApplicationStatusUpdate(user.getEmail(), subject, body);
    }

    @Override
    public void sendSystemAlert(String message) {
        // Implementation for admin alerts
        emailService.sendSystemAlert("admin@portal.gov", "System Alert", message);
    }
}