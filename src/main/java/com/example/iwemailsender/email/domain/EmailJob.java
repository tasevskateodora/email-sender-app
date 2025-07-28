package com.example.iwemailsender.email.domain;

import com.example.iwemailsender.infrastructure.enums.RecurrencePattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "email_jobs")
@Data
public class EmailJob extends BaseEntity{


    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern")
    private RecurrencePattern recurrencePattern;
    private String senderEmail;
    private String receiverEmails;
    private boolean enabled = true;
    private boolean isOneTime = false;
    private LocalDateTime nextRunTime;
    private LocalTime sendTime;


    @OneToMany(mappedBy = "emailJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmailExecution> executions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_template_id")
    private EmailTemplate emailTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private User createdBy;

    public EmailJob(LocalDateTime startDate, LocalDateTime endDate, RecurrencePattern recurrencePattern, String senderEmail, String receiverEmails, boolean enabled, boolean isOneTime, LocalDateTime nextRunTime, LocalTime sendTime) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.recurrencePattern = recurrencePattern;
        this.senderEmail = senderEmail;
        this.receiverEmails = receiverEmails;
        this.enabled = enabled;
        this.isOneTime = isOneTime;
        this.nextRunTime = nextRunTime;
        this.sendTime = sendTime;

    }

    public EmailJob() {
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmails() {
        return receiverEmails;
    }

    public void setReceiverEmails(String receiverEmails) {
        this.receiverEmails = receiverEmails;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isOneTime() {
        return isOneTime;
    }

    public void setOneTime(boolean oneTime) {
        isOneTime = oneTime;
    }

    public LocalDateTime getNextRunTime() {
        return nextRunTime;
    }

    public void setNextRunTime(LocalDateTime nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public LocalTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalTime sendTime) {
        this.sendTime = sendTime;
    }

    public List<EmailExecution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<EmailExecution> executions) {
        this.executions = executions;
    }

    public EmailTemplate getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(EmailTemplate emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}