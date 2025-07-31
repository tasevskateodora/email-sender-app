package com.example.iwemailsender.email.api;

import com.example.iwemailsender.email.dto.EmailJobDto;
import com.example.iwemailsender.email.service.EmailJobService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/email-jobs")
public class EmailJobController {

    private final EmailJobService emailJobService;

    public EmailJobController(EmailJobService emailJobService) {
        this.emailJobService = emailJobService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<EmailJobDto> createEmailJob(
            @PathVariable UUID userId,
            @RequestBody EmailJobDto requestDto) {

        Optional<EmailJobDto> created = emailJobService.save(userId, requestDto);
        return created
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<List<EmailJobDto>> getAllEmailJobs() {
        List<EmailJobDto> jobs = emailJobService.findAll();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailJobDto> getEmailJobById(@PathVariable UUID id) {
        Optional<EmailJobDto> job = emailJobService.findById(id);
        return job.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<EmailJobDto> updateEmailJob(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestBody EmailJobDto requestDto) {

        Optional<EmailJobDto> updated = emailJobService.update(id, userId, requestDto);

        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmailJob(@PathVariable UUID id) {
        emailJobService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmailJobDto>> getEmailJobsByUser(@PathVariable UUID userId) {
        List<EmailJobDto> jobs = emailJobService.findByUserId(userId);
        return ResponseEntity.ok(jobs);
    }

    /*
    @PutMapping("/{id}/toggle")
    public ResponseEntity<EmailJobDto> toggleJobStatus(@PathVariable UUID id) {
        try {
            Optional<EmailJobDto> job = emailJobService.findById(id);

            if (job.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            emailJobService.toggleJobStatus(id);

            Optional<EmailJobDto> updatedJob = emailJobService.findById(id);
            return updatedJob.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PutMapping("/{id}/enable")
    public ResponseEntity<EmailJobDto> enableJob(@PathVariable UUID id) {
        try {
            emailJobService.setJobStatus(id, true);

            Optional<EmailJobDto> updatedJob = emailJobService.findById(id);
            return updatedJob.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PutMapping("/{id}/disable")
    public ResponseEntity<EmailJobDto> disableJob(@PathVariable UUID id) {
        try {
            emailJobService.setJobStatus(id, false);

            Optional<EmailJobDto> updatedJob = emailJobService.findById(id);
            return updatedJob.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/ready-for-execution")
    public ResponseEntity<List<EmailJobDto>> getJobsReadyForExecution() {
        List<EmailJobDto> jobs = emailJobService.findJobsToExecute();
        return ResponseEntity.ok(jobs);
    }
    */

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleJobStatus(@PathVariable UUID id) {
        try {
            emailJobService.toggleJobStatus(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job status toggled successfully");
            response.put("jobId", id);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<Map<String, Object>> enableJob(@PathVariable UUID id) {
        try {
            emailJobService.setJobStatus(id, true);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job enabled successfully");
            response.put("jobId", id);
            response.put("enabled", true);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<Map<String, Object>> disableJob(@PathVariable UUID id) {
        try {
            emailJobService.setJobStatus(id, false);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job disabled successfully");
            response.put("jobId", id);
            response.put("enabled", false);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
