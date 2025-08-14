package com.example.iwemailsender.email.api;

import com.example.iwemailsender.email.dto.EmailExecutionDto;
import com.example.iwemailsender.email.dto.LogExecutionDto;
import com.example.iwemailsender.email.service.EmailExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/email-executions")
@CrossOrigin(origins = "*")
public class EmailExecutionController {

    private final EmailExecutionService emailExecutionService;

    public EmailExecutionController(EmailExecutionService emailExecutionService) {
        this.emailExecutionService = emailExecutionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<EmailExecutionDto>> getAllExecutions() {
        List<EmailExecutionDto> executions = emailExecutionService.findAll();
        return ResponseEntity.ok(executions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<EmailExecutionDto> getExecutionById(@PathVariable UUID id) {
        Optional<EmailExecutionDto> execution = emailExecutionService.findById(id);
        return execution.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<EmailExecutionDto>> getExecutionsByJobId(@PathVariable UUID jobId) {
        List<EmailExecutionDto> executions = emailExecutionService.findByJobId(jobId);
        return ResponseEntity.ok(executions);
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<EmailExecutionDto> createExecution(@Valid @RequestBody EmailExecutionDto executionDto) {
        Optional<EmailExecutionDto> savedExecution = emailExecutionService.save(executionDto);
        return savedExecution.map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/log")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<EmailExecutionDto> logExecution(@Valid @RequestBody LogExecutionDto logDto) {
        Optional<EmailExecutionDto> execution = emailExecutionService.logExecution(logDto);
        return execution.map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .orElse(ResponseEntity.badRequest().build());
    }
}
