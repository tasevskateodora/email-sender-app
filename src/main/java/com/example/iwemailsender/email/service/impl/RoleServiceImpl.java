package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.Role;
import com.example.iwemailsender.email.dto.RoleDto;
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
    public Optional<RoleDto> save(RoleDto roleDto) {
        Role role = roleMapper.toEntity(roleDto);

        if (role.getId() == null && roleRepository.findByName(role.getName()).isPresent()) {
            return Optional.empty();
        }

        Role savedRole = roleRepository.save(role);
        return Optional.of(roleMapper.toDto(savedRole));
    }

    @Override
    public List<RoleDto> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RoleDto> findById(UUID id) {
        return roleRepository.findById(id)
                .map(roleMapper::toDto);
    }

    @Override
    public Optional<RoleDto> update(UUID id, RoleDto request) {
        Optional<Role> existingRoleOpt = roleRepository.findById(id);
        if (existingRoleOpt.isEmpty()) {
            return Optional.empty();
        }

        Role existingRole = existingRoleOpt.get();
        existingRole.setName(request.getName());
        existingRole.setUpdatedAt(LocalDateTime.now());

        Role savedRole = roleRepository.save(existingRole);
        return Optional.of(roleMapper.toDto(savedRole));
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
    public Optional<RoleDto> findByName(String name) {
        return roleRepository.findByName(name)
                .map(roleMapper::toDto);
    }

    @Override
    public Optional<RoleDto> createRole(String name) {
        if (roleRepository.findByName(name).isPresent()) {
            return Optional.empty();
        }
        RoleDto dto = new RoleDto();
        dto.setName(name);
        return save(dto);
    }
}
