package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.dto.EmailJobDto;
import com.example.iwemailsender.email.mapper.EmailJobMapper;
import com.example.iwemailsender.email.repository.EmailJobRepository;
import com.example.iwemailsender.email.repository.EmailTemplateRepository;
import com.example.iwemailsender.email.repository.UserRepository;
import com.example.iwemailsender.email.service.EmailJobService;
import com.example.iwemailsender.infrastructure.enums.RecurrencePattern;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmailJobServiceImpl implements EmailJobService {

    private final EmailJobRepository emailJobRepository;
    private final UserRepository userRepository;
    private final EmailJobMapper emailJobMapper;
    private final EmailTemplateRepository emailTemplateRepository;

    public EmailJobServiceImpl(EmailJobRepository emailJobRepository,
                               UserRepository userRepository,
                               EmailJobMapper emailJobMapper,
                               EmailTemplateRepository emailTemplateRepository) {
        this.emailJobRepository = emailJobRepository;
        this.userRepository = userRepository;
        this.emailJobMapper = emailJobMapper;
        this.emailTemplateRepository = emailTemplateRepository;
    }

    /*
    public Optional<EmailJobDto> save(CreateEmailJobRequestDto request) {
        try {
            EmailJob emailJob = emailJobMapper.toEntity(request);
            emailJob.setRecurrencePattern(request.getRecurrencePattern());
            emailJob.setNextRunTime(request.getStartDate());
            emailJob.setEnabled(true);
            emailJob.setOneTime(false);
            EmailJob saved = emailJobRepository.save(emailJob);
            return Optional.of(emailJobMapper.toDto(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    */

    @Override
    public Optional<EmailJobDto> save(UUID userId, EmailJobDto request) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return Optional.empty();
            }

            EmailJob emailJob = emailJobMapper.toEntity(request);
            emailJob.setRecurrencePattern(request.getRecurrencePattern());
            emailJob.setCreatedBy(userOpt.get());
            emailJob.setNextRunTime(request.getStartDate());
            emailJob.setEnabled(true);
            emailJob.setOneTime(false);

            if (request.getEmailTemplateId() != null) {
                emailTemplateRepository.findById(request.getEmailTemplateId())
                        .ifPresent(emailJob::setEmailTemplate);
            }

            EmailJob saved = emailJobRepository.save(emailJob);
            return Optional.of(emailJobMapper.toDto(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


    @Override
    public List<EmailJobDto> findAll() {
        return emailJobRepository.findAll().stream()
                .map(emailJobMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EmailJobDto> findById(UUID id) {
        return emailJobRepository.findById(id).map(emailJobMapper::toDto);
    }

    @Override
    public Optional<EmailJobDto> update(UUID id, UUID userId, EmailJobDto request) {
        if (!emailJobRepository.existsById(id)) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        EmailJob job = emailJobMapper.toEntity(request);
        job.setId(id);
        job.setCreatedBy(userOpt.get());
        job.setNextRunTime(request.getStartDate());

        EmailJob saved = emailJobRepository.save(job);
        return Optional.of(emailJobMapper.toDto(saved));
    }

    @Override
    public void deleteById(UUID id) {
        emailJobRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailJobDto> findJobsToExecute() {
        return emailJobMapper.toDtoList(emailJobRepository.findJobsToExecute(LocalDateTime.now()));
    }

    @Override
    public void updateNextRunTime(UUID jobId, LocalDateTime nextRunTime) {
        emailJobRepository.findById(jobId).ifPresent(job -> {
            job.setNextRunTime(nextRunTime);
            emailJobRepository.save(job);
        });
    }
    @Override
    public List<EmailJobDto> findByUserId(UUID userId) {
        return emailJobMapper.toDtoList(emailJobRepository.findByCreatedById(userId));
    }
    @Override
    public void setJobStatus(UUID jobId, boolean enabled) {
        Optional<EmailJob> jobOpt = emailJobRepository.findById(jobId);

        if (jobOpt.isPresent()) {
            EmailJob job = jobOpt.get();
            job.setEnabled(enabled);
            emailJobRepository.save(job);
        } else {
            throw new EntityNotFoundException("EmailJob not found with id: " + jobId);
        }
    }
}
