package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.Role;
import com.example.iwemailsender.email.dto.RoleDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleDto dto);

    RoleDto toDto(Role role);

    List<RoleDto> toDtoList(List<Role> roles);
}
