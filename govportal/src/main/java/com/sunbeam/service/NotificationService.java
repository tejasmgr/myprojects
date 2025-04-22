package com.sunbeam.service;

import com.sunbeam.model.User;

public interface NotificationService {
    void sendApplicationUpdate(User user, String applicationId, String status);
    void sendSystemAlert(String message);
}