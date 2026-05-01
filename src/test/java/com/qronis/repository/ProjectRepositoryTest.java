package com.qronis.repository;

import com.qronis.modules.project.domain.entity.Project;
import com.qronis.modules.project.infrastructure.persistence.ProjectRepository;
import com.qronis.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

@Tag("integration")
@Transactional
class ProjectRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;
    private UUID tenantId;
    private UUID userId;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tenant (id, name, created_at, updated_at) VALUES (?, 'Test Tenant', now(), now())",
                tenantId);
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password, name, created_at, updated_at) VALUES (?, 'test@test.com', 'pwd', 'Test User', now(), now())",
                userId);
    }

    @Test
    @DisplayName("findByTenantId paginado: deve retornar todos sem filtro de nome")
    void findByTenantId_paged_returnsAll() {
        projectRepository.save(new Project("Alpha", tenantId, userId));
        projectRepository.save(new Project("Beta", tenantId, userId));

        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> result = projectRepository.findByTenantId(tenantId, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByTenantId paginado: deve filtrar por nome parcial case-insensitive")
    void findByTenantId_paged_filterByName() {
        projectRepository.save(new Project("Alpha", tenantId, userId));
        projectRepository.save(new Project("Beta", tenantId, userId));

        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> result = projectRepository.findByTenantId(tenantId, "alph", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alpha");
    }

    @Test
    @DisplayName("findByTenantId paginado: deve retornar vazio quando filtro não corresponde")
    void findByTenantId_paged_filterByName_noMatch() {
        projectRepository.save(new Project("Alpha", tenantId, userId));

        Pageable pageable = PageRequest.of(0, 20);
        Page<Project> result = projectRepository.findByTenantId(tenantId, "xyz", pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByTenantId: deve retornar projetos do tenant com criador carregado")
    void findByTenantId_success() {
        projectRepository.save(new Project("Alpha", tenantId, userId));
        projectRepository.save(new Project("Beta", tenantId, userId));

        List<Project> result = projectRepository.findByTenantId(tenantId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByTenantId: não deve retornar projetos de outro tenant")
    void findByTenantId_otherTenant() {
        UUID otherTenantId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tenant (id, name, created_at, updated_at) VALUES (?, 'Other Tenant', now(), now())",
                otherTenantId);
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password, name, created_at, updated_at) VALUES (?, 'other@test.com', 'pwd', 'Other User', now(), now())",
                otherUserId);
        projectRepository.save(new Project("Projeto Alheio", otherTenantId, otherUserId));

        List<Project> result = projectRepository.findByTenantId(tenantId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndTenantId: deve encontrar projeto do tenant correto")
    void findByIdAndTenantId_success() {
        Project p = projectRepository.save(new Project("Projeto X", tenantId, userId));

        Optional<Project> result = projectRepository.findByIdAndTenantId(p.getId(), tenantId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Projeto X");
    }

    @Test
    @DisplayName("findByIdAndTenantId: deve retornar vazio para tenant errado")
    void findByIdAndTenantId_wrongTenant() {
        Project p = projectRepository.save(new Project("Projeto X", tenantId, userId));

        Optional<Project> result = projectRepository.findByIdAndTenantId(p.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}
