package com.qronis.service;

import com.qronis.modules.tracker.application.TrackerService;
import com.qronis.modules.tracker.web.dto.TimeEntryPatchRequestDTO;
import com.qronis.modules.tracker.application.TimeEntryMapper;
import com.qronis.modules.tracker.domain.entity.TimeEntry;
import com.qronis.modules.tracker.api.exception.ActiveTimerConflictException;
import com.qronis.modules.tracker.api.exception.InvalidTimeBoundsException;
import com.qronis.modules.tracker.api.exception.TimeEntryNotFoundException;
import com.qronis.modules.tracker.infrastructure.persistence.TimeEntryRepository;
import com.qronis.modules.project.application.ProjectService;

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
class TrackerServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private TimeEntryMapper timeEntryMapper;

    @InjectMocks
    private TrackerService trackerService;

    private UUID tenantId;
    private UUID userId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
    }

    // --- START ---

    @Test
    @DisplayName("start: deve iniciar timer com sucesso")
    void start_success() {
        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntry entry = trackerService.start(projectId, "Feature X", tenantId, userId);

        assertThat(entry.getStartTime()).isNotNull();
        assertThat(entry.getEndTime()).isNull();
        assertThat(entry.getDescription()).isEqualTo("Feature X");
        assertThat(entry.getProjectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("start: deve rejeitar se já existe timer ativo")
    void start_activeTimerExists() {
        TimeEntry active = new TimeEntry();
        active.setStartTime(Instant.now());

        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> trackerService.start(projectId, "Feature X", tenantId, userId))
                .isInstanceOf(ActiveTimerConflictException.class)
                .hasMessageContaining("timer ativo");

        verify(timeEntryRepository, never()).save(any());
    }

    // --- STOP ---

    @Test
    @DisplayName("stop: deve parar timer ativo")
    void stop_success() {
        TimeEntry active = new TimeEntry();
        active.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        active.setUserId(userId);

        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.of(active));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntry result = trackerService.stop(userId);

        assertThat(result.getEndTime()).isNotNull();
        assertThat(result.getEndTime()).isAfter(result.getStartTime());
    }

    @Test
    @DisplayName("stop: deve lançar exceção se não há timer ativo")
    void stop_noActiveTimer() {
        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackerService.stop(userId))
                .isInstanceOf(TimeEntryNotFoundException.class)
                .hasMessageContaining("Nenhum timer ativo");
    }

    // --- CREATE MANUAL ---

    @Test
    @DisplayName("create: deve criar entry manual com start e end")
    void create_success() {
        Instant start = Instant.now().minus(3, ChronoUnit.HOURS);
        Instant end = Instant.now();

        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntry entry = trackerService.create(projectId, "Reunião", start, end, tenantId, userId);

        assertThat(entry.getStartTime()).isEqualTo(start);
        assertThat(entry.getEndTime()).isEqualTo(end);
    }

    @Test
    @DisplayName("create: deve rejeitar endTime antes de startTime")
    void create_invalidTimes() {
        Instant start = Instant.now();
        Instant end = start.minus(1, ChronoUnit.HOURS);

        assertThatThrownBy(() -> trackerService.create(projectId, "Reunião", start, end, tenantId, userId))
                .isInstanceOf(InvalidTimeBoundsException.class)
                .hasMessageContaining("posterior ao de início");
    }

    // --- PATCH ---

    @Test
    @DisplayName("patch: deve atualizar descrição parcialmente")
    void patch_description() {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setUserId(userId);
        entry.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());
        entry.setDescription("Original");

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId))
                .thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO("Atualizada", null, null, null);
        TimeEntry result = trackerService.patch(entry.getId(), request, tenantId, userId);

        assertThat(result.getDescription()).isEqualTo("Atualizada");
    }

    @Test
    @DisplayName("patch: deve rejeitar endTime antes de startTime")
    void patch_invalidTimes() {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setUserId(userId);
        entry.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId))
                .thenReturn(Optional.of(entry));

        Instant badEnd = entry.getStartTime().minus(1, ChronoUnit.HOURS);
        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO(null, null, badEnd, null);

        assertThatThrownBy(() -> trackerService.patch(entry.getId(), request, tenantId, userId))
                .isInstanceOf(InvalidTimeBoundsException.class)
                .hasMessageContaining("posterior ao de início");
    }

    // --- DELETE ---

    @Test
    @DisplayName("delete: deve excluir entry do próprio usuário")
    void delete_success() {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setUserId(userId);

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId))
                .thenReturn(Optional.of(entry));

        trackerService.delete(entry.getId(), userId);

        verify(timeEntryRepository).delete(entry);
    }

    @Test
    @DisplayName("delete: deve rejeitar exclusão de entry de outro usuário")
    void delete_wrongUser() {
        UUID otherUserId = UUID.randomUUID();

        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setUserId(otherUserId);

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackerService.delete(entry.getId(), userId))
                .isInstanceOf(TimeEntryNotFoundException.class);
    }
}
