package com.qronis.modules.identity.api.dto;

import org.springframework.modulith.NamedInterface;
import java.util.UUID;

@NamedInterface("api")
public record IdentityProvisionResult(
    UUID userId,
    String name,
    String email,
    UUID tenantId,
    String role
) {}
