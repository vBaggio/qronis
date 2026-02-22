package com.qronis.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record TimeEntryCreateRequestDTO(
        @NotNull(message = "ID do projeto é obrigatório") UUID projectId,

        String description,

        @NotNull(message = "Horário de início é obrigatório") Instant startTime,

        @NotNull(message = "Horário de término é obrigatório") Instant endTime) {
}
