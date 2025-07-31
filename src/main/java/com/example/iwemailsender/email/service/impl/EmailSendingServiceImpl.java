 package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.config.EmailSchedulerConfig;
import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.service.EmailExecutionService;
import com.example.iwemailsender.email.service.EmailSendingService;
import com.example.iwemailsender.infrastructure.enums.EmailStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


 @Service
public class EmailSendingServiceImpl implements EmailSendingService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSendingServiceImpl.class);

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final EmailSchedulerConfig emailConfig;
    private final EmailExecutionService emailExecutionService;
    public EmailSendingServiceImpl(JavaMailSender mailSender,EmailSchedulerConfig emailConfig, EmailExecutionService emailExecutionService) {
        this.mailSender = mailSender;
        this.objectMapper = new ObjectMapper();
        this.emailConfig = emailConfig;
        this.emailExecutionService = emailExecutionService;
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

        logger.info("Email sending completed. Success: {}", successCount);

        if (failureCount > 0 && successCount == 0) {
            throw new Exception("Failed to send email to all recipients. Last error: " +
                    (lastException != null ? lastException.getMessage() : "Unknown error"));
        }
        if (failureCount > 0) {
            logger.warn("Partial failure: {} emails sent successfully", successCount);
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
    @Retryable(
            value = {Exception.class},
            maxAttemptsExpression = "#{@emailSchedulerConfig.retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "#{@emailSchedulerConfig.retry.delaySeconds}")
    )
    public void sendEmailWithTemplate(UUID jobId, String from, String recipients, EmailTemplate template) throws Exception {
        if (template == null) {
            throw new IllegalArgumentException("EmailTemplate cannot be null");
        }

        if (template.getSubject() == null || template.getBody() == null) {
            throw new IllegalArgumentException("EmailTemplate must have both subject and body");
        }

        logger.info("Sending email with template '{}' for job {}", template.getName(), jobId);

        try {
            sendEmailToMultipleRecipients(from, recipients, template.getSubject(), template.getBody());
        } catch (Exception e) {
            logger.warn("Email sending failed for job {}: {}", jobId, e.getMessage());
            throw e;
        }
    }

     @Recover
     public void recoverSendEmailWithTemplate(Exception e, UUID jobId, String from, String recipients, EmailTemplate template) {
         logger.error("All retry attempts failed for sending email for job {}: {}", jobId, e.getMessage());

         try {
             emailExecutionService.logExecution(jobId, EmailStatus.FAIL, e.getMessage(), emailConfig.getRetry().getMaxAttempts());
         } catch (Exception logEx) {
             logger.error("Failed to log execution failure for job {}: {}", jobId, logEx.getMessage());
         }

         try {
             String subject = "[ALERT] Failed Email Job Notification";
             String body = String.format(
                     "The system failed to send an email after multiple retry attempts.\n\n" +
                             "From: %s\nTo: %s\nTemplate Name: %s\nSubject: %s\n\n" +
                             "Error: %s\n\n",
                     from,
                     recipients,
                     template != null ? template.getName() : "N/A",
                     template != null ? template.getSubject() : "N/A",
                     e.getMessage()
             );

             mailSender.send(mimeMessage -> {
                 MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                 helper.setFrom("system@company.com");
                 helper.setTo(emailConfig.getAdmin().getNotificationEmail());
                 helper.setSubject(subject);
                 helper.setText(body, false);
             });

             logger.info("Admin alert email sent after retry failure.");
         } catch (Exception ex) {
             logger.error("Failed to send admin notification after retries failed: {}", ex.getMessage());
         }

         throw new RuntimeException("Retry failed: " + e.getMessage(), e);
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

