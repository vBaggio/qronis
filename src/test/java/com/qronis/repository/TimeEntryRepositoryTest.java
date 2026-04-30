package com.qronis.repository;

import com.qronis.modules.tracker.domain.entity.TimeEntry;
import com.qronis.modules.tracker.infrastructure.persistence.TimeEntryRepository;
import com.qronis.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TimeEntryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    private UUID userId;
    private UUID projectId;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenant (id, name, created_at, updated_at) VALUES (?, 'Test Tenant', now(), now())", tenantId);
        jdbcTemplate.update("INSERT INTO users (id, email, password, name, created_at, updated_at) VALUES (?, 'test@test.com', 'pwd', 'Test User', now(), now())", userId);
        jdbcTemplate.update("INSERT INTO project (id, tenant_id, created_by, name, created_at, updated_at) VALUES (?, ?, ?, 'Test Project', now(), now())", projectId, tenantId, userId);
    }

    @Test
    @DisplayName("findActiveByUserId: deve encontrar timer ativo com projeto via JOIN FETCH")
    void findActiveByUserId_success() {
        TimeEntry entry = new TimeEntry();
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(Instant.now());
        timeEntryRepository.save(entry);

        Optional<TimeEntry> result = timeEntryRepository.findActiveByUserId(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getProjectId()).isEqualTo(projectId);
        assertThat(result.get().getEndTime()).isNull();
    }

    @Test
    @DisplayName("findActiveByUserId: deve retornar vazio se não há timer ativo")
    void findActiveByUserId_noActive() {
        TimeEntry entry = new TimeEntry();
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());
        timeEntryRepository.save(entry);

        Optional<TimeEntry> result = timeEntryRepository.findActiveByUserId(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_ordered() {
        Instant now = Instant.now();

        TimeEntry older = new TimeEntry();
        older.setProjectId(projectId);
        older.setUserId(userId);
        older.setStartTime(now.minus(5, ChronoUnit.HOURS));
        older.setEndTime(now.minus(4, ChronoUnit.HOURS));
        timeEntryRepository.save(older);

        TimeEntry newer = new TimeEntry();
        newer.setProjectId(projectId);
        newer.setUserId(userId);
        newer.setStartTime(now.minus(2, ChronoUnit.HOURS));
        newer.setEndTime(now.minus(1, ChronoUnit.HOURS));
        timeEntryRepository.save(newer);

        Page<TimeEntry> result = timeEntryRepository.findByUserId(userId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getStartTime()).isAfter(result.getContent().get(1).getStartTime());
    }
}
