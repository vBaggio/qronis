package com.qronis.service;

import com.qronis.modules.identity.api.dto.IdentityProvisionResult;
import com.qronis.modules.identity.api.dto.TenantUserAuthDTO;
import com.qronis.modules.identity.api.exception.UserAlreadyExistsException;
import com.qronis.modules.identity.application.IdentityService;
import com.qronis.modules.identity.domain.entity.Tenant;
import com.qronis.modules.identity.domain.entity.TenantUser;
import com.qronis.modules.identity.domain.entity.TenantUserId;
import com.qronis.modules.identity.domain.entity.User;
import com.qronis.modules.identity.domain.enums.Role;
import com.qronis.modules.identity.infrastructure.persistence.TenantRepository;
import com.qronis.modules.identity.infrastructure.persistence.TenantUserRepository;
import com.qronis.modules.identity.infrastructure.persistence.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private TenantUserRepository tenantUserRepository;

    @InjectMocks
    private IdentityService identityService;

    private User user;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        user = new User("vini@email.com", "encoded", "Vinicius");
        user.setId(UUID.randomUUID());
        tenant = new Tenant("Qronis Ltda");
        tenant.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("provisionTenant: deve criar user, tenant, tenantUser e retornar resultado")
    void provisionTenant_success() {
        when(userRepository.existsByEmail("vini@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantUserRepository.save(any(TenantUser.class))).thenReturn(
                new TenantUser(tenant, user, Role.OWNER));

        IdentityProvisionResult result = identityService.provisionTenant(
                "Vinicius", "vini@email.com", "encoded", "Qronis Ltda");

        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.email()).isEqualTo("vini@email.com");
        assertThat(result.tenantId()).isEqualTo(tenant.getId());
        assertThat(result.role()).isEqualTo("OWNER");
        verify(tenantUserRepository).save(any(TenantUser.class));
    }

    @Test
    @DisplayName("provisionTenant: deve lançar UserAlreadyExistsException para email duplicado")
    void provisionTenant_duplicateEmail() {
        when(userRepository.existsByEmail("vini@email.com")).thenReturn(true);

        assertThatThrownBy(() ->
                identityService.provisionTenant("Vinicius", "vini@email.com", "encoded", "Empresa"))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("getAuthDetailsByEmail: deve retornar DTO quando encontrado")
    void getAuthDetailsByEmail_found() {
        TenantUserId id = new TenantUserId(tenant.getId(), user.getId());
        TenantUser tu = new TenantUser(tenant, user, Role.OWNER);
        tu.setId(id);
        when(tenantUserRepository.findByUserEmailWithUser("vini@email.com"))
                .thenReturn(Optional.of(tu));

        Optional<TenantUserAuthDTO> result = identityService.getAuthDetailsByEmail("vini@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("vini@email.com");
        assertThat(result.get().role()).isEqualTo("OWNER");
    }

    @Test
    @DisplayName("getAuthDetailsByEmail: deve retornar vazio quando email inexistente")
    void getAuthDetailsByEmail_notFound() {
        when(tenantUserRepository.findByUserEmailWithUser("nao@existe.com"))
                .thenReturn(Optional.empty());

        Optional<TenantUserAuthDTO> result = identityService.getAuthDetailsByEmail("nao@existe.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail: deve delegar ao repositório")
    void existsByEmail_delegates() {
        when(userRepository.existsByEmail("vini@email.com")).thenReturn(true);

        assertThat(identityService.existsByEmail("vini@email.com")).isTrue();
    }
}
