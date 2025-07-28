package com.example.iwemailsender.email.service.impl;

import com.example.iwemailsender.email.domain.Role;
import com.example.iwemailsender.email.domain.User;
import com.example.iwemailsender.email.dto.CreateRoleRequestDto;
import com.example.iwemailsender.email.dto.CreateUserRequestDto;
import com.example.iwemailsender.email.dto.UserResponseDto;
import com.example.iwemailsender.email.mapper.UserMapper;
import com.example.iwemailsender.email.repository.RoleRepository;
import com.example.iwemailsender.email.repository.UserRepository;
import com.example.iwemailsender.email.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<UserResponseDto> save(CreateUserRequestDto userRequest) {
        System.out.println("Saving user: " + userRequest.getUsername());
        try {
            if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
                throw new RuntimeException("User with this username already exists.");
            }

            User user = userMapper.toEntity(userRequest);
            if (user.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            user.setEnabled(true);
            if (userRequest.getRoleNames() != null && !userRequest.getRoleNames().isEmpty()) {
                List<Role> roles = userRequest.getRoleNames().stream()
                        .map(roleName -> roleRepository.findByName(roleName)
                                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                        .collect(Collectors.toList());
                user.setRoles(roles);
            }

            User savedUser = userRepository.save(user);
            return Optional.of(userMapper.toResponseDTO(savedUser));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }



    @Override
    public List<UserResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserResponseDto> findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDTO);
    }

    @Override
    public Optional<UserResponseDto> update(UUID id, CreateUserRequestDto userRequest) {
        if (!userRepository.existsById(id)) {
            //return Optional.empty();
            throw new RuntimeException("User with" + id + "not found");
        }
        User user = userMapper.toEntity(userRequest);
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User updatedUser = userRepository.save(user);
        return Optional.of(userMapper.toResponseDTO(updatedUser));
    }

    @Override
    public String deleteById(UUID id) {
        userRepository.deleteById(id);
        return "User with id " + id + " successfully deleted.";
    }

    @Override
    public Optional findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponseDTO);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Optional<UserResponseDto> createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        User savedUser = userRepository.save(user);
        return Optional.of(userMapper.toResponseDTO(savedUser));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void assignRole(UUID userId, String roleName) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Role> roleOpt = roleRepository.findByName(roleName);

        if (userOpt.isPresent() && roleOpt.isPresent()) {
            User user = userOpt.get();
            Role role = roleOpt.get();

            if (!user.getRoles().contains(role)) {
                user.getRoles().add(role);
                userRepository.save(user);
            }
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void removeRole(UUID userId, CreateRoleRequestDto roleRequest) {
        Optional<User> userOpt=userRepository.findById(userId);
        Optional<Role> roleOpt=roleRepository.findByName(roleRequest.getName());
        if (userOpt.isPresent() && roleOpt.isPresent())
        {
            User user=userOpt.get();
            Role role=roleOpt.get();
            user.getRoles().remove(role);
            userRepository.save(user);
        }
    }
}
