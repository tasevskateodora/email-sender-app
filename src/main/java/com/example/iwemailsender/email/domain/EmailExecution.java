package com.example.iwemailsender.email.domain;

import com.example.iwemailsender.infrastructure.enums.EmailStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "email_executions")
public class EmailExecution extends BaseEntity{

    private EmailStatus status;
    private String errorMessage;
    private int retryAttempt;
    private LocalDateTime executedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_job_id", nullable = false)
    //@JsonIgnore
    private EmailJob emailJob;

    public EmailExecution() {
    }

    public EmailExecution(LocalDateTime executedAt, EmailStatus status, String errorMessage, int retryAttempt) {
        this.executedAt = executedAt;
        this.status = status;
        this.errorMessage = errorMessage;
        this.retryAttempt = retryAttempt;

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

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public EmailJob getEmailJob() {
        return emailJob;
    }

    public void setEmailJob(EmailJob emailJob) {
        this.emailJob = emailJob;
    }
}