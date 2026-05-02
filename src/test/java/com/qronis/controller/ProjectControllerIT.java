package com.qronis.controller;

import com.qronis.modules.project.web.dto.ProjectResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
class ProjectControllerIT extends AbstractControllerIT {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String email = "proj-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        token = registerAndGetToken(email, "senha123");
    }

    @Test
    @DisplayName("GET /api/projects: deve retornar 401 sem token")
    void list_unauthorized() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/projects: deve retornar página vazia para tenant sem projetos")
    void list_emptyPage() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/projects: deve criar projeto e retornar 201")
    void create_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto Teste"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Projeto Teste"));
    }

    @Test
    @DisplayName("POST /api/projects: deve retornar 400 para nome em branco")
    void create_blankName() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", ""));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("DELETE /api/projects/{id}: deve retornar 204")
    void delete_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Para Deletar"));
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andReturn();

        ProjectResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProjectResponseDTO.class);
        String projectId = created.id().toString();

        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/projects/{id}: deve retornar 404 para projeto inexistente")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/projects/" + UUID.randomUUID())
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PROJECT_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/projects/{id}: deve retornar 200 com o projeto")
    void getById_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto GetById"));
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andReturn();
        ProjectResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProjectResponseDTO.class);
        String id = created.id().toString();

        mockMvc.perform(get("/api/projects/" + id)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Projeto GetById"));
    }

    @Test
    @DisplayName("GET /api/projects/{id}: deve retornar 404 para projeto de outro tenant")
    void getById_otherTenantIsolation() throws Exception {
        // Cria projeto com tenant A
        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto Tenant A"));
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andReturn();

        ProjectResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProjectResponseDTO.class);
        String projectId = created.id().toString();

        // Tenant B tenta acessar
        String tokenB = registerAndGetToken(
                "tenantb-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com", "senha123");

        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", bearerHeader(tokenB)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/projects/{id}: deve atualizar nome e retornar 200")
    void update_success() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of("name", "Nome Original"));
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andReturn();
        ProjectResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProjectResponseDTO.class);

        String updateBody = objectMapper.writeValueAsString(Map.of("name", "Nome Atualizado"));
        mockMvc.perform(put("/api/projects/" + created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Atualizado"));
    }

    @Test
    @DisplayName("PUT /api/projects/{id}: deve retornar 404 para projeto inexistente")
    void update_notFound() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Qualquer Nome"));
        mockMvc.perform(put("/api/projects/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PROJECT_NOT_FOUND"));
    }
}
