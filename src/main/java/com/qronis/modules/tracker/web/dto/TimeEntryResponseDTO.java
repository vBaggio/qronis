package com.qronis.modules.tracker.web.dto;

import java.time.Instant;
import java.util.UUID;

public record TimeEntryResponseDTO(
        UUID id,
        String description,
        Instant startTime,
        Instant endTime,
        UUID projectId,
        String projectName,
        Instant createdAt) {
}
