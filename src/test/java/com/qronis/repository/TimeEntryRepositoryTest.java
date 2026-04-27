package com.qronis.repository;

import com.qronis.modules.project.domain.entity.Project;
import com.qronis.modules.project.application.repositories.ProjectRepository;
import com.qronis.modules.identity.domain.enums.Role;
import com.qronis.modules.identity.domain.entity.Tenant;
import com.qronis.modules.identity.domain.entity.TenantUser;
import com.qronis.modules.tracker.domain.entity.TimeEntry;
import com.qronis.modules.tracker.application.repositories.TimeEntryRepository;
import com.qronis.modules.identity.domain.entity.User;
import com.qronis.modules.identity.application.repositories.TenantRepository;
import com.qronis.modules.identity.application.repositories.UserRepository;
import com.qronis.modules.identity.application.repositories.TenantUserRepository;
import com.qronis.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TimeEntryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantUserRepository tenantUserRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        Tenant tenant = tenantRepository.save(new Tenant("Qronis Test"));
        user = userRepository.save(new User("test@email.com", "encoded", "Tester"));
        tenantUserRepository.save(new TenantUser(tenant, user, Role.OWNER));
        project = projectRepository.save(new Project("Projeto Test", tenant, user));
    }

    @Test
    @DisplayName("findActiveByUserId: deve encontrar timer ativo com projeto via JOIN FETCH")
    void findActiveByUserId_success() {
        TimeEntry entry = new TimeEntry();
        entry.setProject(project);
        entry.setCreatedBy(user);
        entry.setStartTime(Instant.now());
        timeEntryRepository.save(entry);

        Optional<TimeEntry> result = timeEntryRepository.findActiveByUserId(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getProject().getName()).isEqualTo("Projeto Test");
        assertThat(result.get().getEndTime()).isNull();
    }

    @Test
    @DisplayName("findActiveByUserId: deve retornar vazio se não há timer ativo")
    void findActiveByUserId_noActive() {
        TimeEntry entry = new TimeEntry();
        entry.setProject(project);
        entry.setCreatedBy(user);
        entry.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());
        timeEntryRepository.save(entry);

        Optional<TimeEntry> result = timeEntryRepository.findActiveByUserId(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdWithProject: deve retornar histórico ordenado por startTime desc")
    void findByUserIdWithProject_ordered() {
        Instant now = Instant.now();

        TimeEntry older = new TimeEntry();
        older.setProject(project);
        older.setCreatedBy(user);
        older.setStartTime(now.minus(5, ChronoUnit.HOURS));
        older.setEndTime(now.minus(4, ChronoUnit.HOURS));
        timeEntryRepository.save(older);

        TimeEntry newer = new TimeEntry();
        newer.setProject(project);
        newer.setCreatedBy(user);
        newer.setStartTime(now.minus(2, ChronoUnit.HOURS));
        newer.setEndTime(now.minus(1, ChronoUnit.HOURS));
        timeEntryRepository.save(newer);

        Page<TimeEntry> result = timeEntryRepository.findByUserIdWithProject(user.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getStartTime()).isAfter(result.getContent().get(1).getStartTime());
    }
}
