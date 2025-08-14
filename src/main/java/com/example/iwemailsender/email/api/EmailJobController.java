package com.example.iwemailsender.email.api;

import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.dto.EmailJobDto;
import com.example.iwemailsender.email.repository.UserRepository;
import com.example.iwemailsender.email.service.EmailJobService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/email-jobs")
public class EmailJobController {

    private final EmailJobService emailJobService;
    private final UserRepository userRepository;
    public EmailJobController(EmailJobService emailJobService, UserRepository userRepository) {
        this.emailJobService = emailJobService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/user")
    public ResponseEntity<EmailJobDto> createEmailJob(
            Authentication authentication,
            @RequestBody EmailJobDto requestDto) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UUID userId = user.getId();

        Optional<EmailJobDto> created = emailJobService.save(userId, requestDto);

        return created
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<EmailJobDto>> getAllEmailJobs() {
        List<EmailJobDto> jobs = emailJobService.findAll();
        return ResponseEntity.ok(jobs);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmailJobDto> getEmailJobById(@PathVariable UUID id) {
        Optional<EmailJobDto> job = emailJobService.findById(id);
        return job.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<EmailJobDto> updateEmailJob(
            @PathVariable UUID id,
            @PathVariable String userId,
            @RequestBody EmailJobDto requestDto) {

        UUID actualUserId;

        try {
            actualUserId = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            User user = userRepository.findByUsername(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
            actualUserId = user.getId();
        }

        Optional<EmailJobDto> updated = emailJobService.update(id, actualUserId, requestDto);

        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PreAuthorize("hasRole('ADMIN')")
    //@PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmailJob(@PathVariable UUID id) {
        emailJobService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmailJobDto>> getEmailJobsByUser(@PathVariable String userId) {

        UUID uuid = null;
        try {
            uuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            User user = userRepository.findByUsername(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            uuid = user.getId();
        }

        List<EmailJobDto> jobs = emailJobService.findByUserId(uuid);
        return ResponseEntity.ok(jobs);
    }



    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
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

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
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




