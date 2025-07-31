package com.example.iwemailsender.email.api;

import com.example.iwemailsender.email.dto.RoleDto;
import com.example.iwemailsender.email.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto roleDto) {
        Optional<RoleDto> savedRole = roleService.save(roleDto);
        return savedRole
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<RoleDto> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable UUID id) {
        Optional<RoleDto> role = roleService.findById(id);
        return role
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable UUID id, @RequestBody RoleDto roleDto) {
        Optional<RoleDto> updatedRole = roleService.update(id, roleDto);
        return updatedRole
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        Optional<RoleDto> role = roleService.findById(id);
        if (role.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        roleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
