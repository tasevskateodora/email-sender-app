package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.dto.CreateRoleRequestDto;
import com.example.iwemailsender.email.dto.RoleResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleService {


    Optional<RoleResponseDto> save(CreateRoleRequestDto createRoleRequestDto);
    List<RoleResponseDto> findAll();
    Optional<RoleResponseDto> findById(UUID id);
    Optional<RoleResponseDto> update(UUID id, CreateRoleRequestDto createRoleRequestDto);
    boolean deleteById(UUID id);
    Optional<RoleResponseDto> findByName(String name);
    Optional<RoleResponseDto> createRole(String name);
}
