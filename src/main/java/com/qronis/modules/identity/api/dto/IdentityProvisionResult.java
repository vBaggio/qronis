package com.qronis.modules.identity.api.dto;

import com.qronis.modules.identity.api.enums.Role;
import org.springframework.modulith.NamedInterface;
import java.util.UUID;

@NamedInterface("api")
public record IdentityProvisionResult(
    UUID userId,
    String name,
    String email,
    UUID tenantId,
    Role role
) {}
