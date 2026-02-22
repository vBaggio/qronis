package com.qronis.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponseDTO(
        int status,
        String message,
        Map<String, String> errors,
        Instant timestamp) {
    public ErrorResponseDTO(int status, String message) {
        this(status, message, null, Instant.now());
    }

    public ErrorResponseDTO(int status, String message, Map<String, String> errors) {
        this(status, message, errors, Instant.now());
    }
}
