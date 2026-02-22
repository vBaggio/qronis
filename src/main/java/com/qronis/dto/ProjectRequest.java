package com.qronis.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(
        @NotBlank(message = "Nome do projeto é obrigatório") String name) {
}
