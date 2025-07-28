package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.dto.CreateRoleRequestDto;
import com.example.iwemailsender.email.dto.CreateUserRequestDto;
import com.example.iwemailsender.email.dto.UserResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserService {


    Optional<UserResponseDto> save(CreateUserRequestDto userRequest);
    List<UserResponseDto> findAll();
    Optional<UserResponseDto> findById(UUID id);
    Optional<UserResponseDto> update(UUID id, CreateUserRequestDto userRequest);
    String deleteById(UUID id);
    Optional<UserResponseDto> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<UserResponseDto> createUser(String username, String password);
    void assignRole(UUID userId, String roleName);
    void removeRole(UUID userId, CreateRoleRequestDto roleRequest);

}
