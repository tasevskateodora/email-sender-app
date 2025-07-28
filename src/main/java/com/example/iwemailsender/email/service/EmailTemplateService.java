package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.dto.CreateEmailTemplateRequestDto;
import com.example.iwemailsender.email.dto.EmailTemplateResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateService {

    Optional<EmailTemplateResponseDto> save(CreateEmailTemplateRequestDto requestDto);
    List<EmailTemplateResponseDto> findAll();
    Optional<EmailTemplateResponseDto> findById(UUID id);
    Optional<EmailTemplateResponseDto> update(UUID id, CreateEmailTemplateRequestDto requestDto);
    void deleteById(UUID id);

    Optional<EmailTemplateResponseDto> findByName(String name);

    Optional<EmailTemplateResponseDto> createTemplate(String name, String subject, String body);
}

