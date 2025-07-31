package com.example.iwemailsender.email.dto;

import com.example.iwemailsender.infrastructure.enums.EmailStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailExecutionDto {

    private UUID id;
    private LocalDateTime executedAt;
    private EmailStatus status;
    private String errorMessage;
    private int retryAttempt;
    private LocalDateTime createdAt;
    private UUID emailJobId;
    private String jobSenderEmail;
    private String jobReceiverEmails;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public EmailStatus getStatus() {
        return status;
    }

    public void setStatus(EmailStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryAttempt() {
        return retryAttempt;
    }

    public void setRetryAttempt(int retryAttempt) {
        this.retryAttempt = retryAttempt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getEmailJobId() {
        return emailJobId;
    }

    public void setEmailJobId(UUID emailJobId) {
        this.emailJobId = emailJobId;
    }

    public String getJobSenderEmail() {
        return jobSenderEmail;
    }

    public void setJobSenderEmail(String jobSenderEmail) {
        this.jobSenderEmail = jobSenderEmail;
    }

    public String getJobReceiverEmails() {
        return jobReceiverEmails;
    }

    public void setJobReceiverEmails(String jobReceiverEmails) {
        this.jobReceiverEmails = jobReceiverEmails;
    }
}
