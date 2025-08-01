package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.dto.RoleDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleService {

    Optional<RoleDto> save(RoleDto roleDto);

    List<RoleDto> findAll();

    Optional<RoleDto> findById(UUID id);

    Optional<RoleDto> update(UUID id, RoleDto roleDto);

    boolean deleteById(UUID id);

    Optional<RoleDto> findByName(String name);

}

