package com.qronis.modules.project.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponseDTO(
        UUID id,
        String name,
        UUID tenantId,
        Instant createdAt) {
}
