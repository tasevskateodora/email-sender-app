package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.dto.EmailJobDto;
import com.example.iwemailsender.infrastructure.enums.RecurrencePattern;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailJobService {

    Optional<EmailJobDto> save(UUID userId, EmailJobDto request);
    List<EmailJobDto> findAll();
    Optional<EmailJobDto> findById(UUID id);
    Optional<EmailJobDto> update(UUID id, UUID userId, EmailJobDto request);
    void deleteById(UUID id);
    List<EmailJobDto> findJobsToExecute();
    void updateNextRunTime(UUID jobId, LocalDateTime nextRunTime);
    List<EmailJobDto> findByUserId(UUID userId);
    void setJobStatus(UUID jobId, boolean enabled);
}


