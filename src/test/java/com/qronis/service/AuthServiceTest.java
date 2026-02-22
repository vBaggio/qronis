package com.qronis.service;

import com.qronis.entity.Role;
import com.qronis.entity.Tenant;
import com.qronis.entity.TenantUser;
import com.qronis.entity.User;
import com.qronis.repository.TenantRepository;
import com.qronis.repository.TenantUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
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
        private UserService userService;
        @Mock
        private TenantRepository tenantRepository;
        @Mock
        private TenantUserRepository tenantUserRepository;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private JwtService jwtService;

        @InjectMocks
        private AuthService authService;

        private User user;
        private Tenant tenant;

        @BeforeEach
        void setUp() {
                user = new User("vini@email.com", "encoded-password", "Vinicius");
                user.setId(UUID.randomUUID());

                tenant = new Tenant("Qronis Ltda");
                tenant.setId(UUID.randomUUID());
        }

        @Test
        @DisplayName("register: deve criar user, tenant, tenant_user e retornar JWT")
        void register_success() {
                when(userService.existsByEmail("vini@email.com")).thenReturn(false);
                when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
                when(userService.save(any(User.class))).thenReturn(user);
                when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
                when(jwtService.generateToken(any(User.class), any(UUID.class), any(Role.class)))
                                .thenReturn("jwt-token");

                String token = authService.register("Vinicius", "vini@email.com", "123456", "Qronis Ltda");

                assertThat(token).isEqualTo("jwt-token");
                verify(tenantUserRepository).save(any(TenantUser.class));
                verify(jwtService).generateToken(user, tenant.getId(), Role.OWNER);
        }

        @Test
        @DisplayName("register: deve rejeitar email duplicado")
        void register_duplicateEmail() {
                when(userService.existsByEmail("vini@email.com")).thenReturn(true);

                assertThatThrownBy(() -> authService.register("Vinicius", "vini@email.com", "123456", "Qronis"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Email já cadastrado");

                verify(userService, never()).save(any());
        }

        @Test
        @DisplayName("login: deve autenticar e retornar JWT")
        void login_success() {
                TenantUser tenantUser = new TenantUser(tenant, user, Role.OWNER);

                when(tenantUserRepository.findByUserEmailWithUser("vini@email.com"))
                                .thenReturn(Optional.of(tenantUser));
                when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
                when(jwtService.generateToken(any(User.class), any(UUID.class), any(Role.class)))
                                .thenReturn("jwt-token");

                String token = authService.login("vini@email.com", "123456");

                assertThat(token).isEqualTo("jwt-token");
        }

        @Test
        @DisplayName("login: deve rejeitar email inexistente")
        void login_emailNotFound() {
                when(tenantUserRepository.findByUserEmailWithUser("nao@existe.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> authService.login("nao@existe.com", "123456"))
                                .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("login: deve rejeitar senha incorreta")
        void login_wrongPassword() {
                TenantUser tenantUser = new TenantUser(tenant, user, Role.OWNER);

                when(tenantUserRepository.findByUserEmailWithUser("vini@email.com"))
                                .thenReturn(Optional.of(tenantUser));
                when(passwordEncoder.matches("senha-errada", "encoded-password")).thenReturn(false);

                assertThatThrownBy(() -> authService.login("vini@email.com", "senha-errada"))
                                .isInstanceOf(BadCredentialsException.class);

                verify(jwtService, never()).generateToken(any(), any(), any());
        }
}
