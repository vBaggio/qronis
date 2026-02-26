package com.qronis.service;

import com.qronis.dto.TimeEntryPatchRequestDTO;
import com.qronis.entity.Project;
import com.qronis.entity.Tenant;
import com.qronis.entity.TimeEntry;
import com.qronis.entity.User;
import com.qronis.exception.BusinessException;
import com.qronis.exception.ResourceNotFoundException;
import com.qronis.repository.TimeEntryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private ProjectService projectService;

    @InjectMocks
    private TimeEntryService timeEntryService;

    private UUID tenantId;
    private UUID userId;
    private UUID projectId;
    private Project project;
    private User user;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        Tenant tenant = new Tenant("Qronis");
        tenant.setId(tenantId);

        user = new User();
        user.setId(userId);

        project = new Project("Projeto Alpha", tenant, user);
        project.setId(projectId);
    }

    // --- START ---

    @Test
    @DisplayName("start: deve iniciar timer com sucesso")
    void start_success() {
        when(projectService.findByIdAndTenantId(projectId, tenantId)).thenReturn(project);
        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntry entry = timeEntryService.start(projectId, "Feature X", tenantId, userId);

        assertThat(entry.getStartTime()).isNotNull();
        assertThat(entry.getEndTime()).isNull();
        assertThat(entry.getDescription()).isEqualTo("Feature X");
        assertThat(entry.getProject()).isEqualTo(project);
    }

    @Test
    @DisplayName("start: deve rejeitar se já existe timer ativo")
    void start_activeTimerExists() {
        TimeEntry active = new TimeEntry();
        active.setStartTime(Instant.now());

        when(projectService.findByIdAndTenantId(projectId, tenantId)).thenReturn(project);
        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> timeEntryService.start(projectId, "Feature X", tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("timer ativo");

        verify(timeEntryRepository, never()).save(any());
    }

    // --- STOP ---

    @Test
    @DisplayName("stop: deve parar timer ativo")
    void stop_success() {
        TimeEntry active = new TimeEntry();
        active.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        active.setCreatedBy(user);

        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.of(active));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntry result = timeEntryService.stop(userId);

        assertThat(result.getEndTime()).isNotNull();
        assertThat(result.getEndTime()).isAfter(result.getStartTime());
    }

    @Test
    @DisplayName("stop: deve lançar exceção se não há timer ativo")
    void stop_noActiveTimer() {
        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeEntryService.stop(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nenhum timer ativo");
    }

    // --- CREATE MANUAL ---

    @Test
    @DisplayName("create: deve criar entry manual com start e end")
    void create_success() {
        Instant start = Instant.now().minus(3, ChronoUnit.HOURS);
        Instant end = Instant.now();

        when(projectService.findByIdAndTenantId(projectId, tenantId)).thenReturn(project);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntry entry = timeEntryService.create(projectId, "Reunião", start, end, tenantId, userId);

        assertThat(entry.getStartTime()).isEqualTo(start);
        assertThat(entry.getEndTime()).isEqualTo(end);
    }

    @Test
    @DisplayName("create: deve rejeitar endTime antes de startTime")
    void create_invalidTimes() {
        Instant start = Instant.now();
        Instant end = start.minus(1, ChronoUnit.HOURS);

        assertThatThrownBy(() -> timeEntryService.create(projectId, "Reunião", start, end, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("posterior ao de início");
    }

    // --- PATCH ---

    @Test
    @DisplayName("patch: deve atualizar descrição parcialmente")
    void patch_description() {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setCreatedBy(user);
        entry.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());
        entry.setDescription("Original");

        when(timeEntryRepository.findByIdAndCreatedByIdWithProject(entry.getId(), userId))
                .thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO("Atualizada", null, null, null);
        TimeEntry result = timeEntryService.patch(entry.getId(), request, tenantId, userId);

        assertThat(result.getDescription()).isEqualTo("Atualizada");
    }

    @Test
    @DisplayName("patch: deve rejeitar endTime antes de startTime")
    void patch_invalidTimes() {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setCreatedBy(user);
        entry.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());

        when(timeEntryRepository.findByIdAndCreatedByIdWithProject(entry.getId(), userId))
                .thenReturn(Optional.of(entry));

        Instant badEnd = entry.getStartTime().minus(1, ChronoUnit.HOURS);
        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO(null, null, badEnd, null);

        assertThatThrownBy(() -> timeEntryService.patch(entry.getId(), request, tenantId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("posterior ao de início");
    }

    // --- DELETE ---

    @Test
    @DisplayName("delete: deve excluir entry do próprio usuário")
    void delete_success() {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setCreatedBy(user);

        when(timeEntryRepository.findByIdAndCreatedByIdWithProject(entry.getId(), userId))
                .thenReturn(Optional.of(entry));

        timeEntryService.delete(entry.getId(), userId);

        verify(timeEntryRepository).delete(entry);
    }

    @Test
    @DisplayName("delete: deve rejeitar exclusão de entry de outro usuário")
    void delete_wrongUser() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setCreatedBy(otherUser);

        when(timeEntryRepository.findByIdAndCreatedByIdWithProject(entry.getId(), userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeEntryService.delete(entry.getId(), userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
