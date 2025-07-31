package com.example.iwemailsender.email.dto;

import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.infrastructure.enums.RecurrencePattern;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class EmailJobDto {

    private UUID id;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private RecurrencePattern recurrencePattern;

    @Email
    private String senderEmail;

    @Email
    private String receiverEmails;

    private boolean enabled = true;
    private boolean isOneTime = false;

    private LocalTime sendTime;

    private LocalDateTime nextRunTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String createdByUsername;
    private UUID createdByUserId;

    private String emailTemplateName;
    private UUID emailTemplateId;

    @JsonIgnore
    private EmailTemplate emailTemplate;

    private EmailTemplateDto emailTemplateDto;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalTime sendTime) {
        this.sendTime = sendTime;
    }

    public LocalDateTime getNextRunTime() {
        return nextRunTime;
    }

    public void setNextRunTime(LocalDateTime nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getEmailTemplateName() {
        return emailTemplateName;
    }

    public void setEmailTemplateName(String emailTemplateName) {
        this.emailTemplateName = emailTemplateName;
    }

    public UUID getEmailTemplateId() {
        return emailTemplateId;
    }

    public void setEmailTemplateId(UUID emailTemplateId) {
        this.emailTemplateId = emailTemplateId;
    }

    public EmailTemplate getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(EmailTemplate emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    public EmailTemplateDto getEmailTemplateDto() {
        return emailTemplateDto;
    }

    public void setEmailTemplateDto(EmailTemplateDto emailTemplateDto) {
        this.emailTemplateDto = emailTemplateDto;
    }
}
