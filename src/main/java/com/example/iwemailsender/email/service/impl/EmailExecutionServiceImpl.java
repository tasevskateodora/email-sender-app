package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.EmailExecution;
import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.repository.EmailExecutionRepository;
import com.example.iwemailsender.email.repository.EmailJobRepository;
import com.example.iwemailsender.email.service.EmailExecutionService;
import com.example.iwemailsender.infrastructure.enums.EmailStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailExecutionServiceImpl implements EmailExecutionService {
    private final EmailExecutionRepository emailExecutionRepository;
    private final EmailJobRepository emailJobRepository;
    public EmailExecutionServiceImpl(EmailExecutionRepository emailExecutionRepository, EmailJobRepository emailJobRepository) {
        this.emailExecutionRepository = emailExecutionRepository;
        this.emailJobRepository=emailJobRepository;
    }

    @Override
    public Optional<EmailExecution> save(EmailExecution execution) {
        try {
            return Optional.of(emailExecutionRepository.save(execution));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<EmailExecution> findAll() {
        return emailExecutionRepository.findAll();
    }

    @Override
    public Optional<EmailExecution> findById(UUID id) {
        return emailExecutionRepository.findById(id);
    }

    @Override
    public Optional<EmailExecution> update(UUID id, EmailExecution execution) {
        try {
            if (!emailExecutionRepository.existsById(id)) {
                return Optional.empty();
            }
            execution.setId(id);
            return Optional.of(emailExecutionRepository.save(execution));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(UUID id) {
        emailExecutionRepository.deleteById(id);
    }

    @Override
    public List<EmailExecution> findByJobId(UUID jobId) {
        return emailExecutionRepository.findByEmailJobIdOrderByExecutedAtDesc(jobId);
    }

    @Override
    public Optional<EmailExecution> logExecution(UUID jobId, EmailStatus status, String errorMessage, int retryAttempt) {
        Optional<EmailJob> jobOpt = emailJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return Optional.empty();
        }

        EmailJob job = jobOpt.get();
        EmailExecution execution = new EmailExecution();
        execution.setEmailJob(job);
        execution.setExecutedAt(LocalDateTime.now());
        execution.setStatus(status);
        execution.setErrorMessage(errorMessage);
        execution.setRetryAttempt(retryAttempt);

        return save(execution);
    }

}
