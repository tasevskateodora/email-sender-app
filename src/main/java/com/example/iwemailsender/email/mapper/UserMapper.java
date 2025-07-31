package com.example.iwemailsender.email.mapper;

import com.example.iwemailsender.email.domain.Role;
import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(target = "roles", ignore = true)
    User toEntity(UserDto dto);

    @Mapping(target = "roleNames", expression = "java(mapRoleNames(user.getRoles()))")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateEntityFromDto(UserDto dto, @MappingTarget User user);

    default List<String> mapRoleNames(List<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    List<UserDto> toDtoList(List<User> users);
}

