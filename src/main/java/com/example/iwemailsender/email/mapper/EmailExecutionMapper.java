package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.EmailExecution;
import com.example.iwemailsender.email.domain.EmailJob;
import com.example.iwemailsender.email.dto.EmailExecutionDto;
import com.example.iwemailsender.email.repository.EmailJobRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailExecutionMapper {

    @Mapping(target = "emailJobId", source = "emailJob.id")
    @Mapping(target = "jobSenderEmail", source = "emailJob.senderEmail")
    @Mapping(target = "jobReceiverEmails", source = "emailJob.receiverEmails")
    EmailExecutionDto toResponseDTO(EmailExecution execution);

    List<EmailExecutionDto> toResponseDTOList(List<EmailExecution> executions);


    @Mapping(target = "emailJob", ignore = true)
    @Mapping(target = "id", source = "id")
    EmailExecution toEntity(EmailExecutionDto dto);
}
