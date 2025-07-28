package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.EmailTemplate;
import com.example.iwemailsender.email.dto.CreateEmailTemplateRequestDto;
import com.example.iwemailsender.email.dto.EmailTemplateResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailTemplateMapper {

    EmailTemplate toEntity(CreateEmailTemplateRequestDto dto);
    EmailTemplateResponseDto toResponseDTO(EmailTemplate template);
    CreateEmailTemplateRequestDto toCreateDTO(EmailTemplate template);
    List<EmailTemplateResponseDto> toResponseDTOList(List<EmailTemplate> templates);
}
