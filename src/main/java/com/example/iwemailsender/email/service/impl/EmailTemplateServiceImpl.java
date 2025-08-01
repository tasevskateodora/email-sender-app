package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.dto.EmailTemplateDto;
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
    public Optional<EmailTemplateDto> save(EmailTemplateDto dto) {
        try {
            EmailTemplate template = emailTemplateMapper.toEntity(dto);
            if (template.getId() == null && emailTemplateRepository.findByName(template.getName()).isPresent()) {
                return Optional.empty();
            }
            EmailTemplate saved = emailTemplateRepository.save(template);
            return Optional.of(emailTemplateMapper.toDto(saved));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<EmailTemplateDto> findAll() {
        return emailTemplateRepository.findAll().stream()
                .map(emailTemplateMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EmailTemplateDto> findById(UUID id) {
        return emailTemplateRepository.findById(id)
                .map(emailTemplateMapper::toDto);
    }

    @Override
    public Optional<EmailTemplateDto> update(UUID id, EmailTemplateDto dto) {
        Optional<EmailTemplate> existingOpt = emailTemplateRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        try {
            EmailTemplate existing = existingOpt.get();
            EmailTemplate updatedTemplate = emailTemplateMapper.toEntity(dto);
            updatedTemplate.setId(id);
            updatedTemplate.setCreatedAt(existing.getCreatedAt());
            EmailTemplate saved = emailTemplateRepository.save(updatedTemplate);
            return Optional.of(emailTemplateMapper.toDto(saved));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(UUID id) {
        emailTemplateRepository.deleteById(id);
    }

}
