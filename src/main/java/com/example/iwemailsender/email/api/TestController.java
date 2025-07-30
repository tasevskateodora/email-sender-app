package com.example.iwemailsender.email.api;

import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.repository.EmailJobRepository;
import com.example.iwemailsender.email.repository.EmailTemplateRepository;
import com.example.iwemailsender.email.repository.UserRepository;
import com.example.iwemailsender.email.scheduler.EmailScheduler;
import com.example.iwemailsender.email.service.EmailJobService;
import com.example.iwemailsender.infrastructure.enums.RecurrencePattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/test/scheduler")
public class TestController {

    private final EmailJobRepository emailJobRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final UserRepository userRepository;
    private final EmailScheduler emailScheduler;
    private final EmailJobService emailJobService;

    public TestController(   EmailJobRepository emailJobRepository,
                             EmailTemplateRepository emailTemplateRepository,
                             UserRepository userRepository,
                             EmailScheduler emailScheduler,
                             EmailJobService emailJobService)
    {
        this.emailJobRepository=emailJobRepository;
        this.emailTemplateRepository=emailTemplateRepository;
        this.userRepository=userRepository;
        this.emailScheduler=emailScheduler;
        this.emailJobService=emailJobService;
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupTestJobs() {
        try {

            List<EmailJob> jobsToDelete = emailJobRepository.findAll().stream()
                    .filter(job -> job.getEmailTemplate() == null ||
                            job.getSenderEmail().contains("example.com"))
                    .toList();

            emailJobRepository.deleteAll(jobsToDelete);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test jobs cleaned up successfully!");
            response.put("deletedJobs", jobsToDelete.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to cleanup: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/create-immediate")
    public ResponseEntity<Map<String, Object>> createImmediateJob(
            @RequestParam String senderEmail,
            @RequestParam String recipientEmail) {

        try {

            User testUser = getOrCreateTestUser();
            EmailTemplate template = getOrCreateTestTemplate();

            EmailJob job = new EmailJob();
            job.setSenderEmail(senderEmail);
            job.setReceiverEmails(recipientEmail);
            job.setStartDate(LocalDateTime.now().minusMinutes(1));
            job.setNextRunTime(LocalDateTime.now());
            job.setRecurrencePattern(RecurrencePattern.DAILY);
            job.setEnabled(true);
            job.setOneTime(true);
            job.setSendTime(LocalTime.now());
            job.setEmailTemplate(template);
            job.setCreatedBy(testUser);

            EmailJob savedJob = emailJobRepository.save(job);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Immediate job created! Should execute within 1 minute.");
            response.put("jobId", savedJob.getId());
            response.put("nextRunTime", savedJob.getNextRunTime());
            response.put("senderEmail", savedJob.getSenderEmail());
            response.put("recipientEmail", savedJob.getReceiverEmails());
            response.put("templateName", savedJob.getEmailTemplate().getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create immediate job: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/create-recurring")
    public ResponseEntity<Map<String, Object>> createRecurringJob(
            @RequestParam String senderEmail,
            @RequestParam String recipientEmail,
            @RequestParam(defaultValue = "DAILY") RecurrencePattern recurrence,
            @RequestParam(defaultValue = "1") int delayMinutes) {

        try {
            User testUser = getOrCreateTestUser();
            EmailTemplate template = getOrCreateTestTemplate();

            LocalDateTime startTime = LocalDateTime.now().plusMinutes(delayMinutes);

            EmailJob job = new EmailJob();
            job.setSenderEmail(senderEmail);
            job.setReceiverEmails(recipientEmail);
            job.setStartDate(startTime);
            job.setNextRunTime(startTime);
            job.setRecurrencePattern(recurrence);
            job.setEnabled(true);
            job.setOneTime(false);
            job.setSendTime(startTime.toLocalTime());
            job.setEmailTemplate(template);
            job.setCreatedBy(testUser);

            EmailJob savedJob = emailJobRepository.save(job);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recurring job created! Next run in " + delayMinutes + " minutes.");
            response.put("jobId", savedJob.getId());
            response.put("nextRunTime", savedJob.getNextRunTime());
            response.put("recurrence", savedJob.getRecurrencePattern());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create recurring job: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/trigger/{jobId}")
    public ResponseEntity<Map<String, Object>> triggerJob(@PathVariable UUID jobId) {
        try {
            EmailJob job = emailJobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            job.setNextRunTime(LocalDateTime.now());
            emailJobRepository.save(job);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job scheduled for immediate execution!");
            response.put("jobId", jobId);
            response.put("info", "Job will execute within 1 minute when scheduler runs");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to trigger job: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/job-status/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable UUID jobId) {
        try {
            EmailJob job = emailJobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobId", job.getId());
            response.put("enabled", job.isEnabled());
            response.put("nextRunTime", job.getNextRunTime());
            response.put("senderEmail", job.getSenderEmail());
            response.put("recipientEmail", job.getReceiverEmails());
            response.put("recurrence", job.getRecurrencePattern());
            response.put("templateName", job.getEmailTemplate() != null ? job.getEmailTemplate().getName() : null);
            response.put("executionsCount", job.getExecutions().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get job status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list-jobs")
    public ResponseEntity<Map<String, Object>> listJobs() {
        try {
            List<EmailJob> jobs = emailJobRepository.findAll().stream()
                    .filter(EmailJob::isEnabled)
                    .sorted((a, b) -> a.getNextRunTime().compareTo(b.getNextRunTime()))
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalJobs", jobs.size());
            response.put("jobs", jobs.stream().map(job -> {
                Map<String, Object> jobInfo = new HashMap<>();
                jobInfo.put("id", job.getId());
                jobInfo.put("senderEmail", job.getSenderEmail());
                jobInfo.put("recipientEmail", job.getReceiverEmails());
                jobInfo.put("nextRunTime", job.getNextRunTime());
                jobInfo.put("recurrence", job.getRecurrencePattern());
                jobInfo.put("hasTemplate", job.getEmailTemplate() != null);
                jobInfo.put("templateName", job.getEmailTemplate() != null ? job.getEmailTemplate().getName() : null);
                return jobInfo;
            }).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to list jobs: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/toggle/{jobId}")
    public ResponseEntity<Map<String, Object>> toggleJob(@PathVariable UUID jobId) {
        try {
            EmailJob job = emailJobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            job.setEnabled(!job.isEnabled());
            emailJobRepository.save(job);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", job.isEnabled() ? "Job enabled" : "Job disabled");
            response.put("jobId", jobId);
            response.put("enabled", job.isEnabled());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to toggle job: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable UUID jobId) {
        try {
            emailJobRepository.deleteById(jobId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "🗑️ Job deleted successfully");
            response.put("jobId", jobId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete job: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/jobs-to-execute")
    public ResponseEntity<Map<String, Object>> getJobsToExecute() {
        try {
            var jobs = emailJobService.findJobsToExecute();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Jobs ready for execution");
            response.put("count", jobs.size());
            response.put("jobs", jobs);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get jobs to execute: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    private User getOrCreateTestUser() {
        return userRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    User testUser = new User();
                    testUser.setUsername("test-scheduler-user");
                    testUser.setPassword("test-password");
                    testUser.setEnabled(true);
                    return userRepository.save(testUser);
                });
    }

    private EmailTemplate getOrCreateTestTemplate() {
        return emailTemplateRepository.findAll().stream()
                .filter(template -> "Scheduler Test Template".equals(template.getName()))
                .findFirst()
                .orElseGet(() -> {
                    EmailTemplate template = new EmailTemplate();
                    template.setName("Scheduler Test Template");
                    template.setSubject("Test Email from Scheduler System!");
                    template.setBody(
                            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                                    "<h2 style='color: #333;'>Scheduler Test Email</h2>" +
                                    "<p>This is a test email sent by the Email Scheduler system.</p>" +
                                    "<p><strong>Sent at:</strong> " + LocalDateTime.now() + "</p>" +
                                    "<div style='background: #f0f8ff; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                                    "<h3>Scheduler Status</h3>" +
                                    "<p>If you received this email, the Email Scheduler is working correctly! </p>" +
                                    "</div>" +
                                    "<p style='color: #666; font-size: 12px;'>Best regards,<br/>Email Scheduler System</p>" +
                                    "</div>"
                    );
                    return emailTemplateRepository.save(template);
                });
    }
}
