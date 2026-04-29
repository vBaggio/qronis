package com.qronis.modules.identity.api.dto;

import java.util.UUID;

public record IdentityProvisionResult(
    UUID userId,
    String name,
    String email,
    UUID tenantId,
    String role
) {}
