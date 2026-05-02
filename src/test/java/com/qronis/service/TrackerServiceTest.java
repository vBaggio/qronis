package com.qronis.service;

import com.qronis.modules.tracker.application.TrackerService;
import com.qronis.modules.tracker.application.TimeEntryMapper;
import com.qronis.modules.tracker.web.dto.TimeEntryPatchRequestDTO;
import com.qronis.modules.tracker.web.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.domain.entity.TimeEntry;
import com.qronis.modules.tracker.api.exception.ActiveTimerConflictException;
import com.qronis.modules.tracker.api.exception.InvalidTimeBoundsException;
import com.qronis.modules.tracker.api.exception.TimeEntryNotFoundException;
import com.qronis.modules.tracker.infrastructure.persistence.TimeEntryRepository;
import com.qronis.modules.project.api.ProjectFacade;
import com.qronis.modules.project.api.exception.ProjectNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackerServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private ProjectFacade projectFacade;
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

    // --- Helpers ---

    private void stubMapSingle(UUID forProjectId) {
        when(projectFacade.getProjectNames(Set.of(forProjectId)))
                .thenReturn(Map.of(forProjectId, "Test Project"));
        when(timeEntryMapper.toResponse(any(TimeEntry.class), any()))
                .thenAnswer(inv -> {
                    TimeEntry e = inv.getArgument(0);
                    return new TimeEntryResponseDTO(
                            e.getId(), e.getDescription(), e.getStartTime(), e.getEndTime(),
                            e.getProjectId(), "Test Project", e.getCreatedAt());
                });
    }

    // --- START ---

    @Test
    @DisplayName("start: deve iniciar timer e retornar DTO com campos corretos")
    void start_success() {
        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        stubMapSingle(projectId);

        TimeEntryResponseDTO result = trackerService.start(projectId, "Feature X", tenantId, userId);

        assertThat(result.startTime()).isNotNull();
        assertThat(result.endTime()).isNull();
        assertThat(result.description()).isEqualTo("Feature X");
        assertThat(result.projectId()).isEqualTo(projectId);
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
    @DisplayName("stop: deve setar endTime e retornar DTO com endTime preenchido")
    void stop_success() {
        TimeEntry active = new TimeEntry();
        active.setProjectId(projectId);
        active.setUserId(userId);
        active.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));

        when(timeEntryRepository.findActiveByUserId(userId)).thenReturn(Optional.of(active));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        stubMapSingle(projectId);

        TimeEntryResponseDTO result = trackerService.stop(userId);

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getEndTime()).isNotNull();
        assertThat(captor.getValue().getEndTime()).isAfter(captor.getValue().getStartTime());
        assertThat(result).isNotNull();
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
    @DisplayName("create: deve criar entry manual com start e end corretos")
    void create_success() {
        Instant start = Instant.now().minus(3, ChronoUnit.HOURS);
        Instant end = Instant.now();

        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        stubMapSingle(projectId);

        TimeEntryResponseDTO result = trackerService.create(projectId, "Reunião", start, end, tenantId, userId);

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getStartTime()).isEqualTo(start);
        assertThat(captor.getValue().getEndTime()).isEqualTo(end);
        assertThat(result).isNotNull();
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
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());
        entry.setDescription("Original");

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId))
                .thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        stubMapSingle(projectId);

        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO("Atualizada", null, null, null);
        trackerService.patch(entry.getId(), request, tenantId, userId);

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getDescription()).isEqualTo("Atualizada");
    }

    @Test
    @DisplayName("patch: deve atualizar startTime")
    void patch_updatesStartTime() {
        Instant originalStart = Instant.now().minus(3, ChronoUnit.HOURS);
        Instant newStart = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);

        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(originalStart);
        entry.setEndTime(end);
        entry.setDescription("Desc");

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId))
                .thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        stubMapSingle(projectId);

        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO(null, newStart, null, null);
        trackerService.patch(entry.getId(), request, tenantId, userId);

        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getStartTime()).isEqualTo(newStart);
    }

    @Test
    @DisplayName("patch: deve validar novo projectId contra o tenant antes de aplicar")
    void patch_newProjectId_validTenant() {
        UUID newProjectId = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId))
                .thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        stubMapSingle(newProjectId);

        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO(null, null, null, newProjectId);
        trackerService.patch(entry.getId(), request, tenantId, userId);

        verify(projectFacade).validateProjectBelongsToTenant(newProjectId, tenantId);
        ArgumentCaptor<TimeEntry> captor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getProjectId()).isEqualTo(newProjectId);
    }

    @Test
    @DisplayName("patch: deve rejeitar projectId de outro tenant — vetor de cross-tenant")
    void patch_newProjectId_foreignTenant() {
        UUID foreignProjectId = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId))
                .thenReturn(Optional.of(entry));
        doThrow(new ProjectNotFoundException(foreignProjectId.toString()))
                .when(projectFacade).validateProjectBelongsToTenant(foreignProjectId, tenantId);

        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO(null, null, null, foreignProjectId);

        assertThatThrownBy(() -> trackerService.patch(entry.getId(), request, tenantId, userId))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("patch: sem projectId não deve chamar validateProjectBelongsToTenant")
    void patch_noProjectId_doesNotValidate() {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        entry.setEndTime(Instant.now());
        entry.setDescription("Original");

        when(timeEntryRepository.findByIdAndUserId(entry.getId(), userId))
                .thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        stubMapSingle(projectId);

        TimeEntryPatchRequestDTO request = new TimeEntryPatchRequestDTO("Atualizada", null, null, null);
        trackerService.patch(entry.getId(), request, tenantId, userId);

        verify(projectFacade, never()).validateProjectBelongsToTenant(any(), any());
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
        when(timeEntryRepository.findByIdAndUserId(any(), eq(userId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackerService.delete(UUID.randomUUID(), userId))
                .isInstanceOf(TimeEntryNotFoundException.class);
    }

    // --- FIND PAGE (batch N+1 fix) ---

    @Test
    @DisplayName("findPageByUser: deve resolver nomes em batch — uma única chamada à facade para toda a página")
    void findPageByUser_batchLookup() {
        UUID projectIdA = UUID.randomUUID();
        UUID projectIdB = UUID.randomUUID();

        TimeEntry entryA1 = entryFor(projectIdA);
        TimeEntry entryA2 = entryFor(projectIdA);
        TimeEntry entryB1 = entryFor(projectIdB);

        Pageable pageable = PageRequest.of(0, 20);
        Page<TimeEntry> page = new PageImpl<>(List.of(entryA1, entryA2, entryB1), pageable, 3);

        when(timeEntryRepository.findByUserId(userId, pageable)).thenReturn(page);
        when(projectFacade.getProjectNames(Set.of(projectIdA, projectIdB)))
                .thenReturn(Map.of(projectIdA, "Alpha", projectIdB, "Beta"));
        when(timeEntryMapper.toResponse(any(TimeEntry.class), any()))
                .thenReturn(new TimeEntryResponseDTO(UUID.randomUUID(), null, Instant.now(), null, projectIdA, "Alpha", Instant.now()));

        Page<TimeEntryResponseDTO> result = trackerService.findPageByUser(userId, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
        // Facade chamada exatamente 1 vez com os 2 projectIds distintos — nunca 3 vezes
        verify(projectFacade, times(1)).getProjectNames(Set.of(projectIdA, projectIdB));
        verify(timeEntryMapper, times(3)).toResponse(any(TimeEntry.class), any());
    }

    @Test
    @DisplayName("findPageByUser: deve filtrar por projectId quando informado")
    void findPageByUser_withProjectIdFilter() {
        TimeEntry entry = entryFor(projectId);
        Pageable pageable = PageRequest.of(0, 20);
        Page<TimeEntry> page = new PageImpl<>(List.of(entry), pageable, 1);

        when(timeEntryRepository.findByUserIdAndProjectId(userId, projectId, pageable)).thenReturn(page);
        when(projectFacade.getProjectNames(Set.of(projectId)))
                .thenReturn(Map.of(projectId, "Alpha"));
        when(timeEntryMapper.toResponse(any(TimeEntry.class), any()))
                .thenReturn(new TimeEntryResponseDTO(UUID.randomUUID(), null, Instant.now(), null, projectId, "Alpha", Instant.now()));

        Page<TimeEntryResponseDTO> result = trackerService.findPageByUser(userId, projectId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(timeEntryRepository).findByUserIdAndProjectId(userId, projectId, pageable);
        verify(timeEntryRepository, never()).findByUserId(any(), any());
    }

    @Test
    @DisplayName("findPageByUser: página vazia não deve chamar a facade")
    void findPageByUser_emptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<TimeEntry> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(timeEntryRepository.findByUserId(userId, pageable)).thenReturn(emptyPage);

        Page<TimeEntryResponseDTO> result = trackerService.findPageByUser(userId, null, pageable);

        assertThat(result.getTotalElements()).isZero();
        verify(projectFacade, never()).getProjectNames(any());
    }

    // --- Helpers privados ---

    private TimeEntry entryFor(UUID pId) {
        TimeEntry e = new TimeEntry();
        e.setId(UUID.randomUUID());
        e.setProjectId(pId);
        e.setUserId(userId);
        e.setStartTime(Instant.now());
        return e;
    }
}
