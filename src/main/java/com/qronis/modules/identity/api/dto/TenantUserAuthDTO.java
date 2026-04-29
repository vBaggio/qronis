package com.qronis.modules.identity.api.dto;

import org.springframework.modulith.NamedInterface;
import java.util.UUID;

@NamedInterface("api")
public record TenantUserAuthDTO(
    UUID userId,
    String name,
    String email,
    String encodedPassword,
    UUID tenantId,
    String role
) {}
