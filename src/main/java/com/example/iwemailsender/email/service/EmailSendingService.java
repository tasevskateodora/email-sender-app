package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.domain.EmailTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public interface EmailSendingService {

    void sendEmail(String from, String to, String subject, String body) throws Exception;
    void sendEmailToMultipleRecipients(String from, String recipients, String subject, String body) throws Exception;
   //void sendEmailWithTemplate(String from, String recipients, EmailTemplate template) throws Exception;
   void sendEmailWithTemplate(UUID jobId, String from, String recipients, EmailTemplate template) throws Exception;

}
