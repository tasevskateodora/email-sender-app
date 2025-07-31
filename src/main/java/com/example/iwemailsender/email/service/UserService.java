package com.example.iwemailsender.email.service;

import com.example.iwemailsender.email.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Optional<UserDto> save(UserDto userDto);
    List<UserDto> findAll();
    Optional<UserDto> findById(UUID id);
    Optional<UserDto> update(UUID id, UserDto userDto);
    String deleteById(UUID id);
    Optional<UserDto> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<UserDto> createUser(String username, String password);
    void assignRole(UUID userId, String roleName);
    void removeRole(UUID userId, String roleName);
}

