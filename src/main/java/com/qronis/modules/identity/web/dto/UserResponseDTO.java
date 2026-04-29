package com.qronis.modules.identity.web.dto;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        UUID tenantId,
        String tenantName,
        String role) {
}
