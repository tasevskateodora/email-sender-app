package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.domain.EmailExecution;
import com.example.iwemailsender.email.dto.EmailExecutionDto;
import com.example.iwemailsender.email.dto.LogExecutionDto;
import com.example.iwemailsender.infrastructure.enums.EmailStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailExecutionService {
    Optional<EmailExecutionDto> save(EmailExecutionDto executionDto);
    List<EmailExecutionDto> findAll();
    Optional<EmailExecutionDto> findById(UUID id);
    List<EmailExecutionDto> findByJobId(UUID jobId);
    Optional<EmailExecutionDto> logExecution(LogExecutionDto executionDto);


}
