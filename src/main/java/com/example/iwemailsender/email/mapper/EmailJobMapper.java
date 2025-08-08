package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.dto.EmailJobDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailJobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "emailTemplate", ignore = true)
    @Mapping(target = "executions", ignore = true)
    @Mapping(target = "nextRunTime", source = "startDate")
    @Mapping(target = "receiverEmails", source = "receiverEmails")
    @Mapping(target = "oneTime", source = "oneTime")
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "sendTime", source = "sendTime")
    @Mapping(target = "recurrencePattern", source = "recurrencePattern")
    EmailJob toEntity(EmailJobDto dto);

    @Mapping(target = "createdByUsername", source = "createdBy.username")
    @Mapping(target = "createdByUserId", source = "createdBy.id")
    @Mapping(target = "emailTemplateName", source = "emailTemplate.name")
    @Mapping(target = "emailTemplateId", source = "emailTemplate.id")
   //@Mapping(source = "emailTemplate", target = "emailTemplate")
    EmailJobDto toDto(EmailJob job);

    List<EmailJobDto> toDtoList(List<EmailJob> jobs);
}

