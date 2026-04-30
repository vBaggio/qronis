package com.qronis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qronis.AbstractIntegrationTest;
import com.qronis.modules.auth.web.dto.AuthResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public abstract class AbstractControllerIT extends AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String registerAndGetToken(String email, String password) throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String uniqueName = "User-" + suffix;
        String companyName = "Company-" + suffix;

        String body = objectMapper.writeValueAsString(Map.of(
                "name", uniqueName,
                "email", email,
                "password", password,
                "companyName", companyName
        ));

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        AuthResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponseDTO.class);
        return response.token();
    }

    protected String bearerHeader(String token) {
        return "Bearer " + token;
    }
}
