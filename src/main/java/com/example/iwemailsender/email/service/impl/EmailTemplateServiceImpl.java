package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.dto.CreateEmailTemplateRequestDto;
import com.example.iwemailsender.email.dto.EmailTemplateResponseDto;
import com.example.iwemailsender.email.mapper.EmailTemplateMapper;
import com.example.iwemailsender.email.repository.EmailTemplateRepository;
import com.example.iwemailsender.email.service.EmailTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateMapper emailTemplateMapper;

    public EmailTemplateServiceImpl(EmailTemplateRepository emailTemplateRepository,
                                    EmailTemplateMapper emailTemplateMapper) {
        this.emailTemplateRepository = emailTemplateRepository;
        this.emailTemplateMapper = emailTemplateMapper;
    }

    @Override
    public Optional<EmailTemplateResponseDto> save(CreateEmailTemplateRequestDto requestDto) {
        try {
            EmailTemplate template = emailTemplateMapper.toEntity(requestDto);
            if (template.getId() == null && emailTemplateRepository.findByName(template.getName()).isPresent()) {
                return Optional.empty();
            }
            EmailTemplate saved = emailTemplateRepository.save(template);
            return Optional.of(emailTemplateMapper.toResponseDTO(saved));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<EmailTemplateResponseDto> findAll() {
        return emailTemplateRepository.findAll().stream()
                .map(emailTemplateMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EmailTemplateResponseDto> findById(UUID id) {
        return emailTemplateRepository.findById(id)
                .map(emailTemplateMapper::toResponseDTO);
    }

    @Override
    public Optional<EmailTemplateResponseDto> update(UUID id, CreateEmailTemplateRequestDto requestDto) {
        Optional<EmailTemplate> existingOpt = emailTemplateRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        try {
            EmailTemplate existing = existingOpt.get();
            EmailTemplate updatedTemplate = emailTemplateMapper.toEntity(requestDto);
            updatedTemplate.setId(id);
            updatedTemplate.setCreatedAt(existing.getCreatedAt());
            EmailTemplate saved = emailTemplateRepository.save(updatedTemplate);
            return Optional.of(emailTemplateMapper.toResponseDTO(saved));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    @Override
    public void deleteById(UUID id) {
        emailTemplateRepository.deleteById(id);
    }

    @Override
    public Optional<EmailTemplateResponseDto> findByName(String name) {
        return emailTemplateRepository.findByName(name)
                .map(emailTemplateMapper::toResponseDTO);
    }

    @Override
    public Optional<EmailTemplateResponseDto> createTemplate(String name, String subject, String body) {
        EmailTemplate template = new EmailTemplate();
        template.setName(name);
        template.setSubject(subject);
        template.setBody(body);
        return save(emailTemplateMapper.toCreateDTO(template));
    }
}
