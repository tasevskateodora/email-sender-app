package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.dto.EmailTemplateDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateService {

    Optional<EmailTemplateDto> save(EmailTemplateDto dto);
    List<EmailTemplateDto> findAll();
    Optional<EmailTemplateDto> findById(UUID id);
    Optional<EmailTemplateDto> update(UUID id, EmailTemplateDto dto);
    void deleteById(UUID id);

}

