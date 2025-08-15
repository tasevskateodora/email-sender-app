package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.dto.EmailDto;
import com.example.iwemailsender.email.dto.EmailJobDto;

public interface EmailSendingService {

    void sendEmail(EmailDto dto) throws Exception;
    void sendEmailToMultipleRecipients(EmailDto dto) throws Exception;
   void sendEmailWithTemplate(EmailJobDto job, String from, String recipients, EmailTemplate template) throws Exception;

}
