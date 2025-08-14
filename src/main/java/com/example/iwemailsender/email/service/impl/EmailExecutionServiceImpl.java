package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.EmailExecution;
import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.dto.EmailExecutionDto;
import com.example.iwemailsender.email.dto.LogExecutionDto;
import com.example.iwemailsender.email.mapper.EmailExecutionMapper;
import com.example.iwemailsender.email.repository.EmailExecutionRepository;
import com.example.iwemailsender.email.repository.EmailJobRepository;
import com.example.iwemailsender.email.service.EmailExecutionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailExecutionServiceImpl implements EmailExecutionService {
    private final EmailExecutionRepository emailExecutionRepository;
    private final EmailJobRepository emailJobRepository;
    private final EmailExecutionMapper emailExecutionMapper;
    public EmailExecutionServiceImpl(EmailExecutionRepository emailExecutionRepository, EmailJobRepository emailJobRepository,EmailExecutionMapper emailExecutionMapper) {
        this.emailExecutionRepository = emailExecutionRepository;
        this.emailJobRepository=emailJobRepository;
        this.emailExecutionMapper=emailExecutionMapper;
    }

    @Override
    public Optional<EmailExecutionDto> save(EmailExecutionDto executionDto) {
        try {
            EmailExecution execution = emailExecutionMapper.toEntity(executionDto);

            EmailExecution savedExecution = emailExecutionRepository.save(execution);

            EmailExecutionDto responseDto = emailExecutionMapper.toResponseDTO(savedExecution);
            return Optional.of(responseDto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<EmailExecutionDto> findAll() {
        List<EmailExecution> executions = emailExecutionRepository.findAll();
        return emailExecutionMapper.toResponseDTOList(executions);
    }

    @Override
    public Optional<EmailExecutionDto> findById(UUID id) {
        return emailExecutionRepository.findById(id)
                .map(emailExecutionMapper::toResponseDTO);
    }

    @Override
    public List<EmailExecutionDto> findByJobId(UUID jobId) {
        List<EmailExecution> executions = emailExecutionRepository.findByEmailJobIdOrderByExecutedAtDesc(jobId);
        return emailExecutionMapper.toResponseDTOList(executions);
    }

    @Override
    public Optional<EmailExecutionDto> logExecution(LogExecutionDto dto) {
        Optional<EmailJob> jobOpt = emailJobRepository.findById(dto.getJobId());
        if (jobOpt.isEmpty()) {
            return Optional.empty();
        }

        EmailJob job = jobOpt.get();
        EmailExecution execution = new EmailExecution();
        execution.setEmailJob(job);
        execution.setExecutedAt(LocalDateTime.now());
        execution.setStatus(dto.getStatus());
        execution.setErrorMessage(dto.getErrorMessage());
        execution.setRetryAttempt(dto.getRetryAttempt());

        try {
            EmailExecution savedExecution = emailExecutionRepository.save(execution);
            EmailExecutionDto responseDto = emailExecutionMapper.toResponseDTO(savedExecution);
            return Optional.of(responseDto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
