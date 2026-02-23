package com.qronis.repository;

import com.qronis.entity.Project;
import com.qronis.entity.Role;
import com.qronis.entity.Tenant;
import com.qronis.entity.TenantUser;
import com.qronis.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class ProjectRepositoryTest {

    @SuppressWarnings("resource") // objeto gerenciado pelo testcontainers
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("qronis_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

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
