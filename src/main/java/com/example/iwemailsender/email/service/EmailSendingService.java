package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.domain.EmailTemplate;

public interface EmailSendingService {

    void sendEmail(String from, String to, String subject, String body) throws Exception;
    void sendEmailToMultipleRecipients(String from, String recipients, String subject, String body) throws Exception;
    void sendEmailWithTemplate(String from, String recipients, EmailTemplate template) throws Exception;

}
