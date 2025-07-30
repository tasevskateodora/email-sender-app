package com.example.iwemailsender.email.dto;

import com.example.iwemailsender.infrastructure.enums.RecurrencePattern;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Data
public class CreateEmailJobRequestDto {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private RecurrencePattern recurrencePattern;
    @Email
    private String senderEmail;
    @Email
    private String receiveEmails;
    private boolean enabled=true;
    private boolean isOneTime=false;
    private LocalTime sendTime;

    private EmailTemplateDto emailTemplate;


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

    public String getReceiveEmails() {
        return receiveEmails;
    }

    public void setReceiveEmails(String receiveEmails) {
        this.receiveEmails = receiveEmails;
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
    public EmailTemplateDto getEmailTemplate() {
        return emailTemplate;
    }
    public void setEmailTemplate(EmailTemplateDto emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

}
