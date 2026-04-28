package com.qronis.modules.tracker.web.dto;

import java.util.UUID;

public record ProjectSummaryResponseDTO(
        UUID projectId,
        long totalDurationSeconds) {
}
