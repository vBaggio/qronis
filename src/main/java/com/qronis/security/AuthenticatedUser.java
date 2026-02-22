package com.qronis.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, UUID tenantId, String email, String role) {

    public static AuthenticatedUser fromContext() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new AuthenticatedUser(
                UUID.fromString(jwt.getSubject()),
                UUID.fromString(jwt.getClaimAsString("tenantId")),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("role"));
    }
}
