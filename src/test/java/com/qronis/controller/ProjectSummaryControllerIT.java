package com.qronis.controller;

import com.qronis.modules.project.web.dto.ProjectResponseDTO;
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
class ProjectSummaryControllerIT extends AbstractControllerIT {

    private String token;
    private String projectId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "summary-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        token = registerAndGetToken(email, "senha123");

        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto Summary"));
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
    @DisplayName("GET /api/projects/{id}/summary: deve retornar 0 segundos para projeto sem entries")
    void summary_noEntries() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectId + "/summary")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDurationSeconds").value(0));
    }

    @Test
    @DisplayName("GET /api/projects/{id}/summary: deve retornar totalSeconds acumulado")
    void summary_withEntries() throws Exception {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
        String createBody = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "1 hora",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));
        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects/" + projectId + "/summary")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDurationSeconds").isNumber());
    }

    @Test
    @DisplayName("GET /api/projects/{id}/summary: deve retornar 404 para projeto de outro tenant")
    void summary_otherTenantForbidden() throws Exception {
        String tokenB = registerAndGetToken(
                "summaryb-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com", "senha123");

        mockMvc.perform(get("/api/projects/" + projectId + "/summary")
                        .header("Authorization", bearerHeader(tokenB)))
                .andExpect(status().isNotFound());
    }
}
