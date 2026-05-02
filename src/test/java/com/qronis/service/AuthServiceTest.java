package com.qronis.service;

import com.qronis.modules.auth.application.AuthService;
import com.qronis.modules.auth.application.JwtService;
import com.qronis.modules.identity.api.IdentityFacade;
import com.qronis.modules.identity.api.dto.IdentityProvisionResult;
import com.qronis.modules.identity.api.dto.TenantUserAuthDTO;
import com.qronis.modules.auth.domain.exception.InvalidCredentialsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

        @Mock
        private IdentityFacade identityFacade;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private JwtService jwtService;

        @InjectMocks
        private AuthService authService;

        private UUID userId;
        private UUID tenantId;

        @BeforeEach
        void setUp() {
                userId = UUID.randomUUID();
                tenantId = UUID.randomUUID();
        }

        @Test
        @DisplayName("register: deve criar user, tenant, tenant_user e retornar JWT")
        void register_success() {
                when(identityFacade.provisionTenant(any(), any(), any(), any()))
                                .thenReturn(new IdentityProvisionResult(
                                                userId, "Vinicius", "vini@email.com", tenantId, "OWNER"));
                when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
                when(jwtService.generateToken(any(UUID.class), any(String.class), any(String.class), any(UUID.class),
                                any(String.class)))
                                .thenReturn("jwt-token");

                String token = authService.register("Vinicius", "vini@email.com", "123456", "Qronis Ltda");

                assertThat(token).isEqualTo("jwt-token");
                verify(jwtService).generateToken(userId, "Vinicius", "vini@email.com", tenantId, "OWNER");
        }

        @Test
        @DisplayName("register: deve rejeitar email duplicado")
        void register_duplicateEmail() {
                when(identityFacade.provisionTenant(any(), any(), any(), any()))
                                .thenThrow(new com.qronis.modules.identity.api.exception.UserAlreadyExistsException(
                                                "Email já cadastrado"));

                assertThatThrownBy(() -> authService.register("Vinicius", "vini@email.com", "123456", "Qronis"))
                                .isInstanceOf(com.qronis.modules.identity.api.exception.UserAlreadyExistsException.class)
                                .hasMessageContaining("Email já cadastrado");
        }

        @Test
        @DisplayName("login: deve autenticar e retornar JWT")
        void login_success() {
                when(identityFacade.getAuthDetailsByEmail("vini@email.com"))
                                .thenReturn(Optional.of(new TenantUserAuthDTO(userId, "Vinicius",
                                                "vini@email.com", "encoded-password", tenantId, "OWNER")));
                when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
                when(jwtService.generateToken(any(UUID.class), any(String.class), any(String.class), any(UUID.class),
                                any(String.class)))
                                .thenReturn("jwt-token");

                String token = authService.login("vini@email.com", "123456");

                assertThat(token).isEqualTo("jwt-token");
        }

        @Test
        @DisplayName("login: deve rejeitar email inexistente")
        void login_emailNotFound() {
                when(identityFacade.getAuthDetailsByEmail("nao@existe.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> authService.login("nao@existe.com", "123456"))
                                .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("login: deve rejeitar senha incorreta")
        void login_wrongPassword() {
                when(identityFacade.getAuthDetailsByEmail("vini@email.com"))
                                .thenReturn(Optional.of(new TenantUserAuthDTO(userId, "Vinicius",
                                                "vini@email.com", "encoded-password", tenantId, "OWNER")));
                when(passwordEncoder.matches("senha-errada", "encoded-password")).thenReturn(false);

                assertThatThrownBy(() -> authService.login("vini@email.com", "senha-errada"))
                                .isInstanceOf(InvalidCredentialsException.class);

                verify(jwtService, never()).generateToken(any(), any(), any(), any(), any());
        }
}
