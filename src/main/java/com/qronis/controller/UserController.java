package com.qronis.controller;

import com.qronis.dto.UserResponseDTO;
import com.qronis.entity.TenantUser;
import com.qronis.exception.ResourceNotFoundException;
import com.qronis.repository.TenantUserRepository;
import com.qronis.security.AuthenticatedUser;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final TenantUserRepository tenantUserRepository;

    public UserController(TenantUserRepository tenantUserRepository) {
        this.tenantUserRepository = tenantUserRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me() {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();

        TenantUser tenantUser = tenantUserRepository.findByUserIdWithUserAndTenant(auth.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return ResponseEntity.ok(new UserResponseDTO(
                tenantUser.getUser().getId(),
                tenantUser.getUser().getName(),
                tenantUser.getUser().getEmail(),
                tenantUser.getTenant().getId(),
                tenantUser.getTenant().getName(),
                tenantUser.getRole().name()));
    }
}
