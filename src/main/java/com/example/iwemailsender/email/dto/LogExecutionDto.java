package com.example.iwemailsender.email.dto;

import com.example.iwemailsender.infrastructure.enums.EmailStatus;

import java.util.UUID;

public class LogExecutionDto {

    private UUID jobId;
    private EmailStatus status;
    private String errorMessage;
    private int retryAttempt;

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
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
}
