package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.Role;
import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.dto.CreateUserRequestDto;
import com.example.iwemailsender.email.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {


    User toEntity(CreateUserRequestDto dto);
    @Mapping(target = "roleNames", expression = "java(mapRoleNames(user.getRoles()))")
    UserResponseDto toResponseDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateEntityFromDTO(CreateUserRequestDto dto, @MappingTarget User user);

    default List<String> mapRoleNames(List<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }


    List<UserResponseDto> toResponseDTOList(List<User> users);
}
