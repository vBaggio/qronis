package com.qronis.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends AbstractControllerIT {

    @Test
    @DisplayName("POST /auth/register: deve retornar 200 com token JWT")
    void register_success() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Vinicius",
                "email", "vini-" + suffix + "@test.com",
                "password", "senha123",
                "companyName", "Qronis Ltda"
        ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register: deve retornar 409 para email duplicado")
    void register_duplicateEmail() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "duplicado-" + suffix + "@test.com";

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Vinicius",
                "email", email,
                "password", "senha123",
                "companyName", "Empresa A"
        ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register: deve retornar 400 para campos inválidos")
    void register_invalidFields() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "",
                "email", "nao-e-email",
                "password", "123",
                "companyName", ""
        ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 200 com token JWT")
    void login_success() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "login-" + suffix + "@test.com";

        String registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Login User",
                "email", email,
                "password", "senha123",
                "companyName", "Empresa Login"
        ));
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "senha123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 401 para senha incorreta")
    void login_wrongPassword() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "wrongpass-" + suffix + "@test.com";

        String registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Wrong Pass User",
                "email", email,
                "password", "correta123",
                "companyName", "Empresa WP"
        ));
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", "errada123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 401 para email inexistente")
    void login_emailNotFound() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "inexistente-" + suffix + "@test.com",
                "password", "qualquer123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }
}
