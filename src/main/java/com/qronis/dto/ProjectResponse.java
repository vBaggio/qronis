package com.qronis.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        UUID tenantId,
        String createdByName,
        Instant createdAt) {
}
