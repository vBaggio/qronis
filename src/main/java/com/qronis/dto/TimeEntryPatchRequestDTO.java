package com.qronis.dto;

import java.time.Instant;
import java.util.UUID;

public record TimeEntryPatchRequestDTO(
        String description,
        Instant startTime,
        Instant endTime,
        UUID projectId) {
}
