package com.qronis.modules.identity.api.dto;

import com.qronis.modules.identity.domain.enums.Role;
import java.util.UUID;

public record TenantUserAuthDTO(
    UUID userId,
    String name,
    String email,
    String encodedPassword,
    UUID tenantId,
    Role role
) {}
