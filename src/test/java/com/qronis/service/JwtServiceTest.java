package com.qronis.service;

import com.qronis.modules.auth.application.JwtService;
import com.qronis.modules.auth.config.JwtProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock private JwtEncoder jwtEncoder;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(jwtProperties.getIssuer()).thenReturn("qronis-test");
        when(jwtProperties.getExpirationHours()).thenReturn(1L);
    }

    @Test
    @DisplayName("generateToken: deve chamar encoder e retornar token")
    void generateToken_callsEncoderAndReturnsToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-jwt-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = jwtService.generateToken(
                UUID.randomUUID(), "Vinicius", "vini@email.com",
                UUID.randomUUID(), "OWNER");

        assertThat(token).isEqualTo("mocked-jwt-token");
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    @DisplayName("generateToken: deve incluir claims corretos nos parâmetros de encode")
    void generateToken_includesCorrectClaims() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
            JwtEncoderParameters params = invocation.getArgument(0);
            assertThat(params.getClaims().getSubject()).isEqualTo(userId.toString());
            assertThat((String) params.getClaims().getClaim("email")).isEqualTo("vini@email.com");
            assertThat((String) params.getClaims().getClaim("tenantId")).isEqualTo(tenantId.toString());
            assertThat((String) params.getClaims().getClaim("role")).isEqualTo("OWNER");
            assertThat((String) params.getClaims().getClaim("iss")).isEqualTo("qronis-test");
            return jwt;
        });

        jwtService.generateToken(userId, "Vinicius", "vini@email.com", tenantId, "OWNER");
    }
}
