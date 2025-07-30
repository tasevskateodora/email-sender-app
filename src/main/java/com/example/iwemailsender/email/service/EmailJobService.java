package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.dto.CreateEmailJobRequestDto;
import com.example.iwemailsender.email.dto.EmailJobResponseDto;
import com.example.iwemailsender.infrastructure.enums.RecurrencePattern;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailJobService {


    Optional<EmailJobResponseDto> save(UUID userId, CreateEmailJobRequestDto request);
    List<EmailJobResponseDto> findAll();
    Optional<EmailJobResponseDto> findById(UUID id);
    public Optional<EmailJobResponseDto> update(UUID id, UUID userId, CreateEmailJobRequestDto request);
    void deleteById(UUID id);
    List<EmailJobResponseDto> findJobsToExecute();
    void updateNextRunTime(UUID jobId, LocalDateTime nextRunTime);
    List<EmailJobResponseDto> findByUserId(UUID userId);
    Optional<EmailJobResponseDto> createJob(UUID userId, String senderEmail, String receiverEmails,
                                            RecurrencePattern pattern, LocalDateTime startDate, LocalTime sendTime);
    void toggleJobStatus(UUID jobId);
    void setJobStatus(UUID jobId, boolean enabled);
}


