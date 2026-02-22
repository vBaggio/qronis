package com.qronis.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TimeEntryStartRequestDTO(
        @NotNull(message = "ID do projeto é obrigatório") UUID projectId,

        String description) {
}
