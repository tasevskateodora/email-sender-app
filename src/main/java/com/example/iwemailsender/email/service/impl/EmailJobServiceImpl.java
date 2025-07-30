package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.dto.CreateEmailJobRequestDto;
import com.example.iwemailsender.email.dto.EmailJobResponseDto;
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
    public EmailJobServiceImpl(EmailJobRepository emailJobrepository, UserRepository userRepository, EmailJobMapper emailJobMapper, EmailJobRepository emailJobRepository, EmailTemplateRepository emailTemplateRepository) {
        this.emailJobRepository = emailJobrepository;
        this.userRepository = userRepository;
        this.emailJobMapper = emailJobMapper;
        this.emailTemplateRepository = emailTemplateRepository;
    }
  /*  public Optional<EmailJobResponseDto> save(CreateEmailJobRequestDto request) {
        try {
            EmailJob emailJob = emailJobMapper.toEntity(request);
            emailJob.setRecurrencePattern(request.getRecurrencePattern());
            emailJob.setNextRunTime(request.getStartDate());
            emailJob.setEnabled(true);
            emailJob.setOneTime(false);
          // emailJob.setCreatedBy(emailJob.getCreatedBy());
            EmailJob saved = emailJobRepository.save(emailJob);
            return Optional.of(emailJobMapper.toResponseDTO(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }*/

   public Optional<EmailJobResponseDto> save(UUID userId, CreateEmailJobRequestDto request) {
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

           //EmailJob emailJob = emailJobMapper.toEntity(request);

            if (request.getEmailTemplate() != null && request.getEmailTemplate().getId() != null) {
                emailTemplateRepository.findById(request.getEmailTemplate().getId())
                        .ifPresent(emailJob::setEmailTemplate);
            }

            EmailJob saved = emailJobRepository.save(emailJob);
            return Optional.of(emailJobMapper.toResponseDTO(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<EmailJobResponseDto> findAll() {
        return emailJobRepository.findAll().stream().map(emailJobMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<EmailJobResponseDto> findById(UUID id) {
        return emailJobRepository.findById(id).map(emailJobMapper::toResponseDTO);
    }

    @Override
    public Optional<EmailJobResponseDto> update(UUID id, UUID userId, CreateEmailJobRequestDto request) {
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
        return Optional.of(emailJobMapper.toResponseDTO(saved));
    }

    @Override
    public void deleteById(UUID id) {
        emailJobRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailJobResponseDto> findJobsToExecute() {
        return emailJobMapper.toResponseDTOList(emailJobRepository.findJobsToExecute(LocalDateTime.now()));
    }

    @Override
    public void updateNextRunTime(UUID jobId, LocalDateTime nextRunTime) {
        emailJobRepository.findById(jobId).ifPresent(job -> {
            job.setNextRunTime(nextRunTime);
            emailJobRepository.save(job);
        });
    }

    @Override
    public List<EmailJobResponseDto> findByUserId(UUID userId) {
        return emailJobMapper.toResponseDTOList(emailJobRepository.findByCreatedById(userId));
    }

    @Override
    public Optional<EmailJobResponseDto> createJob(UUID userId, String senderEmail, String receiveEmails, RecurrencePattern pattern, LocalDateTime startDate, LocalTime sendTime) {
        CreateEmailJobRequestDto dto = new CreateEmailJobRequestDto();
        dto.setSenderEmail(senderEmail);
        dto.setReceiveEmails(receiveEmails);
        dto.setRecurrencePattern(pattern);
        dto.setStartDate(startDate);
        dto.setSendTime(sendTime);

        return save(userId,dto);
    }

    @Override
    public void toggleJobStatus(UUID jobId) {
        Optional<EmailJob> jobOpt = emailJobRepository.findById(jobId);

        if (jobOpt.isPresent()) {
            EmailJob job = jobOpt.get();
            job.setEnabled(!job.isEnabled());
            emailJobRepository.save(job);
        } else {
            throw new EntityNotFoundException("EmailJob not found with id: " + jobId);
        }
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


