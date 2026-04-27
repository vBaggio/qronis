package com.qronis.modules.project.api.dto;

import java.util.UUID;

public record ProjectSummaryResponseDTO(
        UUID projectId,
        long totalDurationSeconds) {
}
