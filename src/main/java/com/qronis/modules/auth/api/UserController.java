package com.qronis.modules.auth.api;

import com.qronis.modules.auth.api.dto.UserResponseDTO;
import com.qronis.modules.auth.application.UserService;
import com.qronis.modules.identity.domain.entity.TenantUser;
import com.qronis.modules.identity.application.repositories.TenantUserRepository;
import com.qronis.shared.security.AuthenticatedUser;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final TenantUserRepository tenantUserRepository;

    public UserController(TenantUserRepository tenantUserRepository) {
        this.tenantUserRepository = tenantUserRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        TenantUser tenantUser = tenantUserRepository.findByUserIdWithUserAndTenant(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(new UserResponseDTO(
                tenantUser.getUser().getId(),
                tenantUser.getUser().getName(),
                tenantUser.getUser().getEmail(),
                tenantUser.getTenant().getId(),
                tenantUser.getTenant().getName(),
                tenantUser.getRole().name()));
    }
}
