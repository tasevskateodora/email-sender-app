package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.config.EmailSchedulerConfig;
import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.dto.EmailDto;
import com.example.iwemailsender.email.dto.LogExecutionDto;
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
    public void sendEmail(EmailDto request) throws Exception {
        logger.info("Sending email from {} to {}", request.getFrom(), request.getTo());

        for (String recipient : request.getTo()) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(request.getFrom());
                helper.setTo(recipient);
                helper.setSubject(request.getSubject());
                helper.setText(request.getBody(), true);

                mailSender.send(message);
                logger.info("Email sent successfully to {}", recipient);

            } catch (MessagingException e) {
                logger.error("Failed to send email to {}: {}", recipient, e.getMessage());
                throw new Exception("Failed to send email: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void sendEmailToMultipleRecipients(EmailDto request) throws Exception {
        logger.info("Sending email from {} to multiple recipients", request.getFrom());

        Exception lastException = null;
        int successCount = 0;
        int failureCount = 0;
        for (String recipient : request.getTo()) {
            try {
                sendEmail(request);
                successCount++;
            } catch (Exception e) {
                lastException = e;
                failureCount++;
                logger.warn("Failed to send email to {}: {}", recipient, e.getMessage());
            }
        }

        if (lastException != null) {
            throw new Exception("Some emails failed to send. Last error: " + lastException.getMessage(), lastException);
        }
        logger.info("Email sending completed. Success: {}", successCount);

        if (failureCount > 0 && successCount == 0) {
            throw new Exception("Failed to send email to all recipients. Last error: " +
                    (lastException != null ? lastException.getMessage() : "Unknown error"));
        }
        if (failureCount > 0) {
            logger.warn("Partial failure: {} emails sent successfully", successCount);
        }
        logger.info("Email sending to multiple recipients completed.");
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


    @Retryable(
            value = {Exception.class},
            maxAttemptsExpression = "#{@emailSchedulerConfig.maxAttempts}",
            backoff = @Backoff(delayExpression = "#{@emailSchedulerConfig.delaySeconds}")
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
            List<String> recipientList = parseRecipients(recipients);
            EmailDto dto = new EmailDto(from, recipientList, template.getSubject(), template.getBody());
            sendEmail(dto);
        } catch (Exception e) {
            logger.warn("Email sending failed for job {}: {}", jobId, e.getMessage());
            throw e;
        }
    }

    @Recover
    public void recoverSendEmailWithTemplate(Exception e, UUID jobId, String from, String recipients, EmailTemplate template) {
        logger.error("All retry attempts failed for sending email for job {}: {}", jobId, e.getMessage());

        try {
            LogExecutionDto dto = new LogExecutionDto();
            dto.setJobId(jobId);
            dto.setStatus(EmailStatus.FAIL);
            dto.setErrorMessage(e.getMessage());
            dto.setRetryAttempt(emailConfig.getMaxAttempts());
            emailExecutionService.logExecution(dto);

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
                helper.setTo(emailConfig.getNotificationEmail());
                helper.setSubject(subject);
                helper.setText(body, false);
            });

            logger.info("Admin alert email sent after retry failure.");
        } catch (Exception ex) {
            logger.error("Failed to send admin notification after retries failed: {}", ex.getMessage());
        }

        throw new RuntimeException("Retry failed: " + e.getMessage(), e);
    }


}