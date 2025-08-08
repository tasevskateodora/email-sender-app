package com.example.iwemailsender.email.scheduler;

import com.example.iwemailsender.config.EmailSchedulerConfig;
import com.example.iwemailsender.email.dto.EmailJobDto;
import com.example.iwemailsender.email.dto.LogExecutionDto;
import com.example.iwemailsender.email.service.EmailJobService;
import com.example.iwemailsender.email.service.EmailExecutionService;
import com.example.iwemailsender.email.service.EmailSendingService;
import com.example.iwemailsender.infrastructure.enums.EmailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



@Component
public class EmailScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EmailScheduler.class);

    private final EmailJobService emailJobService;
    private final EmailExecutionService emailExecutionService;
    private final EmailSendingService emailSendingService;

    private final EmailSchedulerConfig emailConfig;

    public EmailScheduler(EmailJobService emailJobService, EmailExecutionService emailExecutionService,
                          EmailSendingService emailSendingService, EmailSchedulerConfig emailSchedulerConfig)
    {
        this.emailJobService=emailJobService;
        this.emailExecutionService=emailExecutionService;
        this.emailSendingService=emailSendingService;
        this.emailConfig=emailSchedulerConfig;
    }

    @Scheduled(fixedRate = 600000)
    public void executeScheduledJobs()
    {
        logger.info("Starting scheduled jobs execution check");
        try{
            List<EmailJobDto> jobsToExecute=emailJobService.findJobsToExecute();
            logger.info("Found {} jobs to execute", jobsToExecute.size());

            for(EmailJobDto job:jobsToExecute)
            {
                executeJob(job);
            }
        }catch(Exception e)
        {
            logger.error("Error while executing scheduled jobs", e);
        }
    }

    private void executeJob(EmailJobDto job) {
        logger.info("Executing job: {} (ID: {})", job.getId(), job.getId());

        // Create execution log entry
        LogExecutionDto executionDto = new LogExecutionDto();
        executionDto.setJobId(job.getId());
        executionDto.setRetryAttempt(emailConfig.getMaxAttempts()); // Will be updated based on outcome

        try {
            if (!isJobValid(job)) {
                logger.warn("Job {} is not valid for execution", job.getId());

                executionDto.setRetryAttempt(1); // No retry for validation errors
                executionDto.setStatus(EmailStatus.FAIL);
                executionDto.setErrorMessage("Job validation failed");
                emailExecutionService.logExecution(executionDto);
                return;
            }

            logger.info("Attempting to send email for job {}", job.getId());

            emailSendingService.sendEmailWithTemplate(
                    job.getId(),
                    job.getSenderEmail(),
                    job.getReceiverEmails(),
                    job.getEmailTemplate()
            );

            logger.info("Email successfully sent for job {}", job.getId());

            executionDto.setRetryAttempt(1);
            executionDto.setStatus(EmailStatus.SUCCESS);
            executionDto.setErrorMessage(null);
            emailExecutionService.logExecution(executionDto);

            logger.info("SUCCESS execution logged for job {}", job.getId());
            handleSuccessfulExecution(job);

        } catch (Exception e) {

            logger.error("Job {} failed completely after all retries: {}", job.getId(), e.getMessage());

            executionDto.setRetryAttempt(emailConfig.getMaxAttempts());
            executionDto.setStatus(EmailStatus.FAIL);
            executionDto.setErrorMessage("Failed after " + emailConfig.getMaxAttempts() + " attempts: " + e.getMessage());
            emailExecutionService.logExecution(executionDto);

            logger.info("FINAL FAILURE execution logged for job {}", job.getId());
            handleFailedExecution(job);
        }
    }
    private boolean isJobValid(EmailJobDto job)
    {
        LocalDateTime now=LocalDateTime.now();
        if(job.getStartDate()!=null && now.isBefore(job.getStartDate()))
        {
            return false;
        }
        if(job.getEndDate()!=null && now.isAfter(job.getEndDate()))
        {
            return false;
        }
        return job.isEnabled();
    }

    private boolean attemptEmailSending(EmailJobDto job) {
        if (job.getEmailTemplate() == null) {
            logger.error("Job {} has no associated EmailTemplate", job.getId());
            LogExecutionDto dto = new LogExecutionDto();
            dto.setJobId(job.getId());
            dto.setStatus(EmailStatus.FAIL);
            dto.setErrorMessage("No EmailTemplate associated");
            dto.setRetryAttempt(1);

            emailExecutionService.logExecution(dto);
            return false;
        }

        try {
            logger.info("Attempting to send email for job {}", job.getId());
            emailSendingService.sendEmailWithTemplate(
                    job.getId(),
                    job.getSenderEmail(),
                    job.getReceiverEmails(),
                    job.getEmailTemplate()
            );

            LogExecutionDto dto = new LogExecutionDto();
            dto.setJobId(job.getId());
            dto.setStatus(EmailStatus.SUCCESS);
            dto.setErrorMessage(null);
            dto.setRetryAttempt(1);

            emailExecutionService.logExecution(dto);
            return true;
        } catch (Exception e) {
            logger.warn("Email sending failed for job {}: {}", job.getId(), e.getMessage());
            return false;
        }
    }

    private void handleSuccessfulExecution(EmailJobDto job)
    {
        logger.info("Successfully executed job {}", job.getId());

        LocalDateTime nextRunTime=calculateNextRunTime(job);
        if(nextRunTime!=null)
        {
            emailJobService.updateNextRunTime(job.getId(),nextRunTime);
            logger.info("Next execution for job {} scheduled at {}", job.getId(), nextRunTime);
        } else {
            logger.info("Job {} completed (one-time or end date reached)", job.getId());
        }
    }

    private void handleFailedExecution(EmailJobDto job)
    {
        logger.warn("Failed to execute job {} after {} attempts", job.getId(), emailConfig.getMaxAttempts());

        LocalDateTime nextRunTime=calculateNextRunTime(job);
        if(nextRunTime!=null)
        {
            nextRunTime=nextRunTime.plusMinutes(30);
            emailJobService.updateNextRunTime(job.getId(),nextRunTime);
        }
    }

    private LocalDateTime calculateNextRunTime(EmailJobDto job) {
        LocalDateTime now = LocalDateTime.now();

        if (job.isOneTime()) {
            return null;
        }

        if (job.getEndDate() != null && now.isAfter(job.getEndDate())) {
            return null;
        }

        LocalDateTime nextRun = now;
        switch (job.getRecurrencePattern()) {
            case DAILY:
                nextRun = nextRun.plusDays(1);
                break;
            case WEEKLY:
                nextRun = nextRun.plusWeeks(1);
                break;
            case MONTHLY:
                nextRun = nextRun.plusMonths(1);
                break;
            case YEARLY:
                nextRun = nextRun.plusYears(1);
                break;
            case ONE_TIME:

                return null;
            default:
                logger.warn("Unknown recurrence pattern for job {}: {}", job.getId(), job.getRecurrencePattern());
                return null;
        }

        if (job.getSendTime() != null) {
            nextRun = nextRun.with(job.getSendTime());
        }
        if (job.getEndDate() != null && nextRun.isAfter(job.getEndDate())) {
            return null;
        }

        return nextRun;
    }

}