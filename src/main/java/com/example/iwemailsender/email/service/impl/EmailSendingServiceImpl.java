 package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.service.EmailSendingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


@Service
public class EmailSendingServiceImpl implements EmailSendingService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSendingServiceImpl.class);

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public EmailSendingServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.objectMapper = new ObjectMapper();
    }


    @Override
    public void sendEmail(String from, String to, String subject, String body) throws Exception {
        logger.info("Sending email from {} to {}", from, to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            logger.info("Email sent successfully from {} to {}", from, to);

        } catch (MessagingException e) {
            logger.error("Failed to send email from {} to {}: {}", from, to, e.getMessage());
            throw new Exception("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email from {} to {}: {}", from, to, e.getMessage());
            throw new Exception("Unexpected error sending email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailToMultipleRecipients(String from, String recipients, String subject, String body) throws Exception {
        logger.info("Sending email from {} to multiple recipients", from);

        List<String> recipientList = parseRecipients(recipients);
        Exception lastException = null;
        int successCount = 0;
        int failureCount = 0;

        for (String recipient : recipientList) {
            try {
                sendEmail(from, recipient.trim(), subject, body);
                successCount++;
            } catch (Exception e) {
                lastException = e;
                failureCount++;
                logger.warn("Failed to send email to {}: {}", recipient, e.getMessage());
            }
        }

        logger.info("Email sending completed. Success: {}, Failures: {}", successCount, failureCount);

        if (failureCount > 0 && successCount == 0) {
            throw new Exception("Failed to send email to all recipients. Last error: " +
                    (lastException != null ? lastException.getMessage() : "Unknown error"));
        }
        if (failureCount > 0) {
            logger.warn("Partial failure: {} emails sent successfully, {} failed", successCount, failureCount);
        }
    }

    private List<String> parseRecipients(String recipients) {
        if (recipients == null || recipients.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipients cannot be empty");
        }

        try {
            if (recipients.trim().startsWith("[") && recipients.trim().endsWith("]")) {
                return objectMapper.readValue(recipients, new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            logger.debug("Recipients is not JSON array, treating as comma-separated: {}", e.getMessage());
        }
        return Arrays.asList(recipients.split("[,;]"));
    }


  /*  private boolean isValidEmail(String email) {
        return email != null &&
                email.trim().length() > 0 &&
                email.contains("@") &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }*/

/*    public void sendHtmlEmail(String from, String to, String subject, String htmlBody, String textBody) throws Exception
    {
        logger.info("Sending HTML email from {} to {}", from, to);
        try
        {
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);

            if(textBody !=null && !textBody.trim().isEmpty())
            {
                helper.setText(textBody, true);
            } else
            {
                helper.setText(htmlBody, true);
            }
            mailSender.send(message);
            logger.info("HTML email sent successfully from {} to {}", from, to);
        } catch (MessagingException e)
        {
            logger.error("Failed to send HTML email from {} to {}: {}", from, to, e.getMessage());
            throw new Exception("Failed to send HTML email: " + e.getMessage(), e);
        }
    }*/

    public void sendEmailWithAttachment(String from, String to, String subject, String body,
                                        String attachmentPath, String attachmentName) throws Exception
    {
        logger.info("Sending email with attachment from {} to {}", from, to);
        try{
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            if(attachmentPath != null && !attachmentPath.trim().isEmpty())
            {
                helper.addAttachment(attachmentName, new java.io.File(attachmentPath));
            }
            mailSender.send(message);
            logger.info("Email with attachment sent successfully from {} to {}", from, to);
        } catch (MessagingException e) {
        logger.error("Failed to send email with attachment from {} to {}: {}", from, to, e.getMessage());
        throw new Exception("Failed to send email with attachment: " + e.getMessage(), e);
    }
    }

    public boolean testConnection() {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            logger.info("Email server connection test successful");
            return true;
        } catch (Exception e) {
            logger.error("Email server connection test failed: {}", e.getMessage());
            return false;
        }
    }
    @Override
    public void sendEmailWithTemplate(String from, String recipients, EmailTemplate template) throws Exception {
        if (template == null) {
            throw new IllegalArgumentException("EmailTemplate cannot be null");
        }

        if (template.getSubject() == null || template.getBody() == null) {
            throw new IllegalArgumentException("EmailTemplate must have both subject and body");
        }

        logger.info("Sending email with template '{}' from {} to recipients", template.getName(), from);

        sendEmailToMultipleRecipients(from, recipients, template.getSubject(), template.getBody());
    }

    public void sendBulkEmails(String from, List<String> recipients, String subject, String body,
                               int delayBetweenEmails) throws Exception {
        logger.info("Starting bulk email sending to {} recipients", recipients.size());

        int sent = 0;
        for (String recipient : recipients) {
            try {
                sendEmail(from, recipient.trim(), subject, body);
                sent++;

                if (delayBetweenEmails > 0 && sent < recipients.size()) {
                    Thread.sleep(delayBetweenEmails);
                }

            } catch (Exception e) {
                logger.warn("Failed to send bulk email to {}: {}", recipient, e.getMessage());
            }
        }

        logger.info("Bulk email sending completed. Sent {} out of {} emails", sent, recipients.size());
    }

}

