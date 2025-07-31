package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.dto.EmailTemplateDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailTemplateMapper {

    EmailTemplate toEntity(EmailTemplateDto dto);

    EmailTemplateDto toDto(EmailTemplate template);

    List<EmailTemplateDto> toDtoList(List<EmailTemplate> templates);
}

