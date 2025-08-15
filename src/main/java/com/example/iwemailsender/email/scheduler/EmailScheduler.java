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

        try {

            logger.info("Attempting to send email for job {}", job.getId());

            emailSendingService.sendEmailWithTemplate(
                    job,
                    job.getSenderEmail(),
                    job.getReceiverEmails(),
                    job.getEmailTemplate()
            );

            logger.info("Email successfully sent for job {}", job.getId());

            logger.info("SUCCESS execution logged for job {}", job.getId());


        } catch (Exception e) {

            logger.error("Job {} failed completely after all retries: {}", job.getId(), e.getMessage());
            logger.info("FINAL FAILURE execution logged for job {}", job.getId());

        }
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
                    job,
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

}