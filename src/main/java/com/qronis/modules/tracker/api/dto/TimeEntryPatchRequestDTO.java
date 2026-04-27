package com.qronis.modules.tracker.api.dto;

import java.time.Instant;
import java.util.UUID;

public record TimeEntryPatchRequestDTO(
        String description,
        Instant startTime,
        Instant endTime,
        UUID projectId) {
}
