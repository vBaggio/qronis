package com.qronis.shared.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Utilitário para extrair dados do usuário autenticado a partir do SecurityContext.
 *
 * Preferir @AuthenticationPrincipal Jwt jwt como parâmetro de método no controller
 * quando possível — mais explícito e mais fácil de testar.
 *
 * Este helper continua disponível para cenários onde a injeção via parâmetro
 * não é prática (ex: filtros, interceptors, camadas intermediárias).
 */
public record AuthenticatedUser(UUID userId, UUID tenantId, String email, String role) {

    public static AuthenticatedUser fromContext() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return new AuthenticatedUser(
                UUID.fromString(jwt.getSubject()),
                UUID.fromString(jwt.getClaimAsString("tenantId")),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("role"));
    }
}
