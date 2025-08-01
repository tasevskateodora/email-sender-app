package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.dto.EmailDto;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public interface EmailSendingService {

    void sendEmail(EmailDto dto) throws Exception;
    void sendEmailToMultipleRecipients(EmailDto dto) throws Exception;
   void sendEmailWithTemplate(UUID jobId, String from, String recipients, EmailTemplate template) throws Exception;

}
