package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.Role;
import com.example.iwemailsender.email.dto.CreateRoleRequestDto;
import com.example.iwemailsender.email.dto.RoleResponseDto;
import com.example.iwemailsender.email.mapper.RoleMapper;
import com.example.iwemailsender.email.repository.RoleRepository;
import com.example.iwemailsender.email.service.RoleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public Optional<RoleResponseDto> save(CreateRoleRequestDto createRoleRequestDto) {
        Role role = roleMapper.toEntity(createRoleRequestDto);

        if (role.getId() == null && roleRepository.findByName(role.getName()).isPresent()) {
            return Optional.empty();
        }

        Role savedRole = roleRepository.save(role);
        return Optional.of(roleMapper.toResponseDTO(savedRole));
    }

    @Override
    public List<RoleResponseDto> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RoleResponseDto> findById(UUID id) {
        return roleRepository.findById(id)
                .map(roleMapper::toResponseDTO);
    }

    @Override
    public Optional<RoleResponseDto> update(UUID id, CreateRoleRequestDto request) {
        Optional<Role> existingRoleOpt = roleRepository.findById(id);
        if (existingRoleOpt.isEmpty()) {
            return Optional.empty();
        }

        Role existingRole = existingRoleOpt.get();
        existingRole.setName(request.getName());
        existingRole.setUpdatedAt(LocalDateTime.now());

        Role savedRole = roleRepository.save(existingRole);
        return Optional.of(roleMapper.toResponseDTO(savedRole));
    }


    @Override
    public boolean deleteById(UUID id) {
        if (!roleRepository.existsById(id)) {
            return false;
        }
        roleRepository.deleteById(id);
        return true;
    }

    @Override
    public Optional<RoleResponseDto> findByName(String name) {
        return roleRepository.findByName(name)
                .map(roleMapper::toResponseDTO);
    }

    @Override
    public Optional<RoleResponseDto> createRole(String name) {
        if (roleRepository.findByName(name).isPresent()) {
            return Optional.empty();
        }
        CreateRoleRequestDto dto = new CreateRoleRequestDto();
        dto.setName(name);
        return save(dto);
    }
}
