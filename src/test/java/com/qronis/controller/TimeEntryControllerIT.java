package com.qronis.controller;

import com.qronis.modules.project.web.dto.ProjectResponseDTO;
import com.qronis.modules.tracker.web.dto.TimeEntryResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
class TimeEntryControllerIT extends AbstractControllerIT {

    private String token;
    private String projectId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "tracker-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        token = registerAndGetToken(email, "senha123");

        // Cria projeto para usar nos testes
        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto Tracker"));
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andReturn();
        ProjectResponseDTO created = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDTO.class);
        projectId = created.id().toString();
    }

    @Test
    @DisplayName("POST /api/time-entries/start: deve iniciar timer e retornar 201")
    void start_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Trabalhando em feature"
        ));

        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.endTime").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/time-entries/start: deve retornar 409 quando timer já está ativo")
    void start_conflictWhenAlreadyActive() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Primeiro timer"
        ));

        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ACTIVE_TIMER_CONFLICT"));
    }

    @Test
    @DisplayName("PUT /api/time-entries/stop: deve parar timer ativo e retornar endTime preenchido")
    void stop_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Timer para parar"
        ));
        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/time-entries/stop")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endTime").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/time-entries: deve criar entry manual com startTime e endTime")
    void create_manualEntry() throws Exception {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);

        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Entry manual",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").isNotEmpty())
                .andExpect(jsonPath("$.endTime").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/time-entries: deve retornar 400 para bounds inválidos (start >= end)")
    void create_invalidBounds() throws Exception {
        Instant now = Instant.now();
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Bounds inválidos",
                "startTime", now.toString(),
                "endTime", now.minus(1, ChronoUnit.HOURS).toString()
        ));

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_TIME_BOUNDS"));
    }

    @Test
    @DisplayName("PATCH /api/time-entries/{id}: deve atualizar description")
    void patch_description() throws Exception {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
        String createBody = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Original",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));
        MvcResult createResult = mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andReturn();
        TimeEntryResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), TimeEntryResponseDTO.class);
        String entryId = created.id().toString();

        String patchBody = objectMapper.writeValueAsString(Map.of("description", "Atualizado"));

        mockMvc.perform(patch("/api/time-entries/" + entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Atualizado"));
    }

    @Test
    @DisplayName("DELETE /api/time-entries/{id}: deve retornar 204")
    void delete_success() throws Exception {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
        String createBody = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Para deletar",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));
        MvcResult createResult = mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andReturn();
        TimeEntryResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), TimeEntryResponseDTO.class);
        String entryId = created.id().toString();

        mockMvc.perform(delete("/api/time-entries/" + entryId)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/time-entries/{id}: deve retornar 404 para entry inexistente")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/time-entries/" + UUID.randomUUID())
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("TIME_ENTRY_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/time-entries/start: deve retornar 400 para projectId nulo")
    void start_missingProjectId_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "description", "Sem projeto"
        ));

        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.projectId").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/time-entries/active: deve retornar 200 com o timer ativo")
    void active_withActiveTimer() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Timer ativo"
        ));
        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/time-entries/active")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.endTime").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/time-entries/active: deve retornar 204 quando não há timer ativo")
    void active_noActiveTimer() throws Exception {
        mockMvc.perform(get("/api/time-entries/active")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/time-entries: deve retornar histórico paginado")
    void history_paged() throws Exception {
        Instant start = Instant.now().minus(3, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(2, ChronoUnit.HOURS);
        String createBody = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Entry histórico",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));
        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/time-entries")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
