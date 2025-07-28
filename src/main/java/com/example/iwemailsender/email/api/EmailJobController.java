package com.example.iwemailsender.email.api;

import com.example.iwemailsender.email.dto.CreateEmailJobRequestDto;
import com.example.iwemailsender.email.dto.EmailJobResponseDto;
import com.example.iwemailsender.infrastructure.enums.RecurrencePattern;
import com.example.iwemailsender.email.service.EmailJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email-jobs")
public class EmailJobController {

    private final EmailJobService emailJobService;

    public EmailJobController(EmailJobService emailJobService) {
        this.emailJobService = emailJobService;
    }

 /*   @PostMapping("/email-job")
    public ResponseEntity<EmailJobResponseDto> createGlobalEmailJob(@RequestBody CreateEmailJobRequestDto requestDto) {
        Optional<EmailJobResponseDto> created = emailJobService.save(requestDto);

        return created
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }*/

    @PostMapping("/user/{userId}")
   public ResponseEntity<EmailJobResponseDto> createEmailJob(
            @PathVariable UUID userId,
          @RequestBody CreateEmailJobRequestDto requestDto) {
       Optional<EmailJobResponseDto> created = emailJobService.save(userId, requestDto);
      return created
               .map(ResponseEntity::ok)
              .orElseGet(() -> ResponseEntity.badRequest().build());
    }
    @GetMapping
    public ResponseEntity<List<EmailJobResponseDto>> getAllEmailJobs() {
        List<EmailJobResponseDto> jobs = emailJobService.findAll();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailJobResponseDto> getEmailJobById(@PathVariable UUID id) {
        Optional<EmailJobResponseDto> job = emailJobService.findById(id);
        return job.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<EmailJobResponseDto> updateEmailJob(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestBody CreateEmailJobRequestDto requestDto) {

        Optional<EmailJobResponseDto> updated = emailJobService.update(id, userId, requestDto);

        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmailJob(@PathVariable UUID id) {
        emailJobService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

   @GetMapping("/user/{userId}")
   public ResponseEntity<List<EmailJobResponseDto>> getEmailJobsByUser(@PathVariable UUID userId) {
       List<EmailJobResponseDto> jobs = emailJobService.findByUserId(userId);
       return ResponseEntity.ok(jobs);
     }
}

