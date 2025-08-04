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
            if (!isJobValid(job)) {
                logger.warn("Job {} is not valid for execution", job.getId());
                return;
            }
            boolean success = attemptEmailSending(job);
            if (success) {
                handleSuccessfulExecution(job);
            } else {
                handleFailedExecution(job);
            }
        } catch (Exception e) {
            logger.error("Unexpected error executing job {}: {}", job.getId(), e.getMessage(), e);
            LogExecutionDto dto = new LogExecutionDto();
            dto.setJobId(job.getId());
            dto.setStatus(EmailStatus.FAIL);
            dto.setErrorMessage("Unexpected error: " + e.getMessage());
            dto.setRetryAttempt(1);

            emailExecutionService.logExecution(dto);

        }
    }


  /*  private void executeJob(EmailJobResponseDto job) {
        logger.info("Executing job: {} (ID: {})", job.getId(), job.getId());

        try {
            if (!isJobValid(job)) {
                logger.warn("Job {} is not valid for execution", job.getId());
                return;
            }

            emailSendingService.sendEmailWithTemplate(
                    job.getSenderEmail(),
                    job.getReceiverEmails(),
                    job.getEmailTemplate()
            );

            emailExecutionService.logExecution(job.getId(), EmailStatus.SUCCESS, null, 1);
            handleSuccessfulExecution(job);

        } catch (Exception e) {
            logger.warn("Email sending failed for job {}: {}", job.getId(), e.getMessage());
            emailExecutionService.logExecution(job.getId(), EmailStatus.FAIL, e.getMessage(), 1);
            handleFailedExecution(job);
        }
    }*/

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

/*   private boolean hasExceededMaxRetries(EmailJobResponseDto job)
    {
        List<EmailExecution> recentExecutions= emailExecutionService.findByJobId(job.getId());
        if(recentExecutions.size() < emailConfig.getRetry().getMaxAttempts())
        {
            return false;
        }
        int consecutiveFailures=0;
        for (int i=0;i<Math.min(emailConfig.getRetry().getMaxAttempts(), recentExecutions.size());i++)
        {
            if(recentExecutions.get(i).getStatus()==EmailStatus.FAIL)
            {
                consecutiveFailures++;
            }else{
                break;
            }
        }
        return consecutiveFailures>=emailConfig.getRetry().getMaxAttempts();
    }*/

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


/*    private boolean attemptEmailSending(EmailJobResponseDto job)
    {
        int attempt=1;
        if(job.getEmailTemplate()==null)
        {
            logger.error("Job {} does not have an associated EmailTemplate", job.getId());
            emailExecutionService.logExecution(job.getId(), EmailStatus.FAIL,
                    "No EmailTemplate associated with this job", 1);
            return false;
        }

        while(attempt<=emailConfig.getRetry().getMaxAttempts())
        {
            try {
                logger.info("Sending email for job {} (attempt {})", job.getId(), attempt);

                emailSendingService.sendEmailWithTemplate(
                        job.getSenderEmail(),
                        job.getReceiverEmails(),
                        job.getEmailTemplate()
                );
                emailExecutionService.logExecution(job.getId(), EmailStatus.SUCCESS, null, attempt);
                logger.info("Email sent successfully for job {} on attempt {}", job.getId(), attempt);
                return true;
        }catch (Exception e)
            {
                logger.warn("Failed to send email for job {} on attempt {}: {}",
                        job.getId(), attempt, e.getMessage());

                emailExecutionService.logExecution(job.getId(), EmailStatus.FAIL, e.getMessage(), attempt);
                if (attempt < emailConfig.getRetry().getMaxAttempts()) {
                    try {
                        Thread.sleep(emailConfig.getRetry().getDelaySeconds() * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Retry delay interrupted for job {}", job.getId());
                        break;
                    }
                }

                attempt++;
            }
        }
        return false;
    } */


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
            default:
                logger.warn("Unknown recurrence pattern for job {}: {}",
                        job.getId(), job.getRecurrencePattern());
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

   /* private void notifyAdmin(EmailJobResponseDto job) {
        try {
            String subject = "Email Job Failure Alert - Job: " + job.getId();
            String templateInfo = job.getEmailTemplate() != null ?
                    job.getEmailTemplate().getName() : "No template";

            String body = String.format(
                    "Email job '%s' (ID: %s) has failed %d consecutive times and has been paused.\n\n" +
                            "Job Details:\n" +
                            "- Template: %s\n" +
                            "- Sender: %s\n" +
                            "- Recipients: %s\n" +
                            "- Recurrence: %s\n" +
                            "- Last execution attempts: %d\n\n" +
                            "Please check the job configuration and email logs for more details.",
                    job.getId(), job.getId(), emailConfig.getRetry().getMaxAttempts(),
                    templateInfo, job.getSenderEmail(), job.getReceiverEmails(),
                    job.getRecurrencePattern(), emailConfig.getRetry().getMaxAttempts()
            );

            emailSendingService.sendEmail("system@company.com", emailConfig.getAdmin().getNotificationEmail(), subject, body);
            logger.info("Admin notification sent for failed job {}", job.getId());

        } catch (Exception e) {
            logger.error("Failed to send admin notification for job {}: {}", job.getId(), e.getMessage());
        }
    }*/

   /* public void triggerExecutionDate(UUID jobId)
    {
        logger.info("Manually triggering execution for job {}", jobId);

        emailJobService.findById(jobId).ifPresentOrElse(this::executeJob,
                () -> logger.warn("Job {} not found for manual execution", jobId));
    }*/
}