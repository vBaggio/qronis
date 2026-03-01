package com.qronis.repository;

import com.qronis.entity.Project;
import com.qronis.entity.Role;
import com.qronis.entity.Tenant;
import com.qronis.entity.TenantUser;
import com.qronis.entity.User;
import com.qronis.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TenantUserRepository tenantUserRepository;

    private Tenant tenant;
    private User user;

    @BeforeEach
    void setUp() {
        tenant = tenantRepository.save(new Tenant("Qronis Test"));
        user = userRepository.save(new User("test@email.com", "encoded", "Tester"));
        tenantUserRepository.save(new TenantUser(tenant, user, Role.OWNER));
    }

    @Test
    @DisplayName("findByTenantIdWithCreator paginado: deve retornar todos sem filtro de nome")
    void findByTenantIdWithCreator_paged_returnsAll() {
        projectRepository.save(new Project("Alpha", tenant, user));
        projectRepository.save(new Project("Beta", tenant, user));

        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> result = projectRepository.findByTenantIdWithCreator(tenant.getId(), null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getCreatedBy().getName()).isEqualTo("Tester");
    }

    @Test
    @DisplayName("findByTenantIdWithCreator paginado: deve filtrar por nome parcial case-insensitive")
    void findByTenantIdWithCreator_paged_filterByName() {
        projectRepository.save(new Project("Alpha", tenant, user));
        projectRepository.save(new Project("Beta", tenant, user));

        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> result = projectRepository.findByTenantIdWithCreator(tenant.getId(), "alph", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alpha");
    }

    @Test
    @DisplayName("findByTenantIdWithCreator paginado: deve retornar vazio quando filtro não corresponde")
    void findByTenantIdWithCreator_paged_filterByName_noMatch() {
        projectRepository.save(new Project("Alpha", tenant, user));

        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> result = projectRepository.findByTenantIdWithCreator(tenant.getId(), "xyz", pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByTenantIdWithCreator: deve retornar projetos do tenant com criador carregado")
    void findByTenantIdWithCreator_success() {
        projectRepository.save(new Project("Alpha", tenant, user));
        projectRepository.save(new Project("Beta", tenant, user));

        List<Project> result = projectRepository.findByTenantIdWithCreator(tenant.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCreatedBy().getName()).isEqualTo("Tester");
    }

    @Test
    @DisplayName("findByTenantIdWithCreator: não deve retornar projetos de outro tenant")
    void findByTenantIdWithCreator_otherTenant() {
        Tenant other = tenantRepository.save(new Tenant("Outro Tenant"));
        User otherUser = userRepository.save(new User("other@email.com", "encoded", "Outro"));
        tenantUserRepository.save(new TenantUser(other, otherUser, Role.OWNER));
        projectRepository.save(new Project("Projeto Alheio", other, otherUser));

        List<Project> result = projectRepository.findByTenantIdWithCreator(tenant.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndTenantIdWithCreator: deve encontrar projeto do tenant correto")
    void findByIdAndTenantIdWithCreator_success() {
        Project p = projectRepository.save(new Project("Projeto X", tenant, user));

        Optional<Project> result = projectRepository.findByIdAndTenantIdWithCreator(p.getId(), tenant.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Projeto X");
    }

    @Test
    @DisplayName("findByIdAndTenantIdWithCreator: deve retornar vazio para tenant errado")
    void findByIdAndTenantIdWithCreator_wrongTenant() {
        Project p = projectRepository.save(new Project("Projeto X", tenant, user));

        Optional<Project> result = projectRepository.findByIdAndTenantIdWithCreator(p.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}
