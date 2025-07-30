package com.example.iwemailsender.config;

import org.springframework.beans.factory.annotation.Value;

public class Admin{

    @Value("${admin.notification-email}")
    private String notificationEmail;

    public String getNotificationEmail()
    {
        return notificationEmail;
    }
    public void setNotificationEmail(String notificationEmail)
    {
        this.notificationEmail=notificationEmail;
    }
}