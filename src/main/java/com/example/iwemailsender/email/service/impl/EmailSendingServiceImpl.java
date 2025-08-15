package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.config.EmailSchedulerConfig;
import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.dto.EmailDto;
import com.example.iwemailsender.email.dto.EmailExecutionDto;
import com.example.iwemailsender.email.dto.EmailJobDto;
import com.example.iwemailsender.email.dto.LogExecutionDto;
import com.example.iwemailsender.email.service.EmailExecutionService;
import com.example.iwemailsender.email.service.EmailJobService;
import com.example.iwemailsender.email.service.EmailSendingService;
import com.example.iwemailsender.infrastructure.enums.EmailStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
public class EmailSendingServiceImpl implements EmailSendingService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSendingServiceImpl.class);

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final EmailSchedulerConfig emailConfig;
    private final EmailExecutionService emailExecutionService;
    private final EmailJobService emailJobService;
    public EmailSendingServiceImpl(JavaMailSender mailSender, EmailSchedulerConfig emailConfig, EmailExecutionService emailExecutionService, EmailJobService emailJobService) {
        this.mailSender = mailSender;
        this.emailJobService = emailJobService;
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


    @Retryable(
            value = {Exception.class},
            maxAttemptsExpression = "#{@emailSchedulerConfig.maxAttempts}",
            backoff = @Backoff(delayExpression = "#{@emailSchedulerConfig.delaySeconds}")
    )
    public void sendEmailWithTemplate(EmailJobDto job, String from, String recipients, EmailTemplate template) throws Exception {

        logger.info("Validating email job {}", job);

        if (template == null) {
            logger.error("EmailTemplate is null for job {}", job);
            throw new IllegalArgumentException("EmailTemplate cannot be null - job configuration error");
        }

        if (template.getSubject() == null || template.getBody() == null) {
            logger.error("EmailTemplate incomplete for job {}: subject={}, body={}",
                    job, template.getSubject(), template.getBody());
            throw new IllegalArgumentException("EmailTemplate must have both subject and body");
        }

        if (recipients == null || recipients.trim().isEmpty()) {
            logger.error("No recipients specified for job {}", job);
            throw new IllegalArgumentException("Recipients cannot be empty");
        }

        if (from == null || from.trim().isEmpty()) {
            logger.error("No sender email specified for job {}", job);
            throw new IllegalArgumentException("Sender email cannot be empty");
        }

        logger.info("Validation passed, sending email with template '{}' for job {}", template.getName(), job);

        try {

            EmailDto dto = new EmailDto(from, parseRecipients(recipients), template.getSubject(), template.getBody());
            sendEmail(dto);

            emailJobService.updateNextRunTime(job.getId(), emailJobService.calculateNextRunTime(job));
            emailExecutionService.logExecution(createDto(job.getId(), 1, EmailStatus.SUCCESS,  null));

            logger.info("Email sent successfully for job {}", job);

        } catch (Exception e) {
            logger.warn("Email sending failed for job {} (attempt will retry): {}", job, e.getMessage());
            throw e;
        }
    }

    @Recover
    public void recoverSendEmailWithTemplate(Exception e, EmailJobDto job, String from, String recipients, EmailTemplate template) {
        logger.error("All retry attempts failed for job {}: {}", job.getId(), e.getMessage());

        try {
            emailExecutionService.logExecution(createDto(job.getId(), emailConfig.getMaxAttempts(), EmailStatus.FAIL, e.getMessage()));
            logger.info("FAIL status logged to database for job {}", job.getId());
        } catch (Exception dbEx) {
            logger.error("CRITICAL: Failed to log FAIL status to database for job {}: {}", job.getId(), dbEx.getMessage());
        }
        try {
            emailJobService.updateNextRunTime(job.getId(), emailJobService.calculateNextRunTime(job));
            logger.info("Next run time updated for job {}", job.getId());
        } catch (Exception updateEx) {
            logger.error("Failed to update next run time for job {}: {}", job.getId(), updateEx.getMessage());
        }

        String failureType;
        String actionRequired;

        if (e instanceof IllegalArgumentException) {
            failureType = "CONFIGURATION ERROR";
            actionRequired = "Fix job configuration:\n" +
                    "- Verify email template is assigned\n" +
                    "- Check template has subject and body\n" +
                    "- Verify sender/receiver emails";
        } else {
            failureType = "SMTP/TECHNICAL ERROR";
            actionRequired = "Fix technical issue:\n" +
                    "- Check SMTP server connectivity\n" +
                    "- Verify email credentials\n" +
                    "- Check network connectivity\n" +
                    "- Review SMTP server logs";
        }

        try {
            String subject = String.format("[%s] Email Job Failed After %d Attempts - ID: %s",
                    failureType, emailConfig.getMaxAttempts(), job.getId());

            String templateInfo = template != null ?
                    String.format("%s (%s)", template.getName(), template.getSubject()) :
                    "Template was NULL";

            String body = String.format(
                    "EMAIL JOB FAILURE ALERT\n\n" +
                            "----------------------------------\n" +
                            "FAILURE TYPE: %s\n" +
                            "----------------------------------\n\n" +
                            "Job ID: %s\n" +
                            "From: %s\n" +
                            "To: %s\n" +
                            "Template: %s\n\n" +
                            "Error: %s\n\n" +
                            "Attempts Made: %d\n" +
                            "Retry Delay: %d seconds\n\n" +
                            "-----------------------------------\n" +
                            "ACTION REQUIRED:\n" +
                            "------------------------------------\n" +
                            "%s\n\n" +
                            "System: Email Scheduler\n" +
                            "Timestamp: %s\n",
                    failureType,
                    job.getId(),
                    from,
                    recipients,
                    templateInfo,
                    e.getMessage(),
                    emailConfig.getMaxAttempts(),
                    emailConfig.getDelaySeconds(),
                    actionRequired,
                    LocalDateTime.now()
            );

            mailSender.send(mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom("teodoratasevska13@gmail.com");
                helper.setTo(emailConfig.getNotificationEmail());
                helper.setSubject(subject);
                helper.setText(body, false);
            });

            logger.info("Admin alert sent for failed job {}", job.getId());

        } catch (Exception ex) {
            logger.error("CRITICAL: Failed to send admin notification for job {}: {}", job.getId(), ex.getMessage());
            System.err.println("CRITICAL: Admin alert failed!");
            System.err.println("Job ID: " + job.getId());
            System.err.println("Failure: " + failureType + " - " + e.getMessage());
            System.err.println("Alert error: " + ex.getMessage());
        }

        throw new RuntimeException("Email job failed after " + emailConfig.getMaxAttempts() +
                " retry attempts: " + e.getMessage(), e);
    }

    LogExecutionDto createDto(UUID jobId, int retryAttempt, EmailStatus status, String errorMessage) {
        LogExecutionDto dto = new LogExecutionDto();
        dto.setJobId(jobId);
        dto.setRetryAttempt(retryAttempt);
        dto.setStatus(status);
        dto.setErrorMessage(errorMessage);
        return dto;
    }

}