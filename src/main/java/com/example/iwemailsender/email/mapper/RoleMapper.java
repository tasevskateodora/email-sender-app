package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.Role;
import com.example.iwemailsender.email.dto.CreateRoleRequestDto;
import com.example.iwemailsender.email.dto.RoleResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(CreateRoleRequestDto dto);
    RoleResponseDto toResponseDTO(Role role);
    List<RoleResponseDto> toResponseDTOList(List<Role> roles);
}
