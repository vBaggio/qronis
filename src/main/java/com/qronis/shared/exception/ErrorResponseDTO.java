package com.qronis.shared.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Contrato padrão de resposta de erro da API.
 *
 * Campos:
 * - status:    código HTTP numérico
 * - error:     código de erro em UPPER_SNAKE_CASE (ex: "PROJECT_NOT_FOUND")
 * - message:   mensagem legível voltada ao desenvolvedor/usuário
 * - errors:    mapa de erros por campo (usado em VALIDATION_ERROR)
 * - timestamp: momento do erro (UTC)
 */
public record ErrorResponseDTO(
        int status,
        String error,
        String message,
        Map<String, String> errors,
        Instant timestamp) {

    /** Erro simples sem código específico — mantém retrocompatibilidade. */
    public ErrorResponseDTO(int status, String message) {
        this(status, errorCodeFromStatus(status), message, null, Instant.now());
    }

    /** Erro semântico com código explícito (padrão dos handlers por módulo). */
    public static ErrorResponseDTO of(int status, String error, String message) {
        return new ErrorResponseDTO(status, error, message, null, Instant.now());
    }

    /** Erro de validação com mapa de campos inválidos. */
    public static ErrorResponseDTO ofValidation(Map<String, String> fieldErrors) {
        return new ErrorResponseDTO(400, "VALIDATION_ERROR",
                "Erro de validação nos campos enviados", fieldErrors, Instant.now());
    }

    private static String errorCodeFromStatus(int status) {
        return switch (status) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 500 -> "INTERNAL_SERVER_ERROR";
            default  -> "ERROR";
        };
    }
}
