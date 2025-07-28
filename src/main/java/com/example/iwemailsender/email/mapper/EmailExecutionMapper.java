package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.EmailExecution;
import com.example.iwemailsender.email.dto.EmailExecutionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailExecutionMapper {

    @Mapping(target = "emailJobId", source = "emailJob.id")
    @Mapping(target = "jobSenderEmail", source = "emailJob.senderEmail")
    @Mapping(target = "jobReceiverEmails", source = "emailJob.receiverEmails")
    EmailExecutionResponseDto toResponseDTO(EmailExecution execution);

    List<EmailExecutionResponseDto> toResponseDTOList(List<EmailExecution> executions);
}
