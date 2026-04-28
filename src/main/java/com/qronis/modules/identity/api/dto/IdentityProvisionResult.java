package com.qronis.modules.identity.api.dto;

import com.qronis.modules.identity.domain.enums.Role;
import java.util.UUID;

public record IdentityProvisionResult(
    UUID userId,
    String name,
    String email,
    UUID tenantId,
    Role role
) {}
