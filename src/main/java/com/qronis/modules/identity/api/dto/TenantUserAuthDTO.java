package com.qronis.modules.identity.api.dto;

import java.util.UUID;

public record TenantUserAuthDTO(
    UUID userId,
    String name,
    String email,
    String encodedPassword,
    UUID tenantId,
    String role
) {}
