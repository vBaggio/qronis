package com.qronis.dto;

import java.util.UUID;

public record ProjectSummaryResponseDTO(
        UUID projectId,
        long totalDurationSeconds) {
}
