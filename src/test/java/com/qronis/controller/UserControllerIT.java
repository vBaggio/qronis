package com.qronis.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
class UserControllerIT extends AbstractControllerIT {

    @Test
    @DisplayName("GET /api/users/me: deve retornar dados do usuário autenticado")
    void me_success() throws Exception {
        String email = "me-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String token = registerAndGetToken(email, "senha123");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.tenantId").isNotEmpty())
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    @DisplayName("GET /api/users/me: deve retornar 401 sem token")
    void me_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
