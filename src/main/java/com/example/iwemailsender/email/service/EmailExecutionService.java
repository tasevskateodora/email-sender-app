package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.domain.EmailExecution;
import com.example.iwemailsender.email.dto.LogExecutionDto;
import com.example.iwemailsender.infrastructure.enums.EmailStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailExecutionService {

    Optional<EmailExecution> save(EmailExecution execution);
    List<EmailExecution> findAll();
    Optional<EmailExecution> findById(UUID id);
    //Optional<EmailExecution> update(UUID id, EmailExecution execution);
    //void deleteById(UUID id);
    List<EmailExecution> findByJobId(UUID jobId);
    Optional<EmailExecution> logExecution(LogExecutionDto executionDto);
}
