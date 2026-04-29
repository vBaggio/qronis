package com.qronis.modules.tracker.application;

import com.qronis.modules.tracker.web.dto.TimeEntryPatchRequestDTO;
import com.qronis.modules.tracker.web.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.domain.entity.TimeEntry;
import com.qronis.modules.tracker.api.exception.ActiveTimerConflictException;
import com.qronis.modules.tracker.api.exception.InvalidTimeBoundsException;
import com.qronis.modules.tracker.api.exception.TimeEntryNotFoundException;
import com.qronis.modules.tracker.infrastructure.persistence.TimeEntryRepository;
import com.qronis.modules.tracker.api.TrackerFacade;
import com.qronis.modules.project.api.ProjectFacade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrackerService implements TrackerFacade {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectFacade projectFacade;
    private final TimeEntryMapper timeEntryMapper;

    public TrackerService(TimeEntryRepository timeEntryRepository,
                          ProjectFacade projectFacade,
                          TimeEntryMapper timeEntryMapper) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectFacade = projectFacade;
        this.timeEntryMapper = timeEntryMapper;
    }

    @Transactional
    public TimeEntryResponseDTO start(UUID projectId, String description, UUID tenantId, UUID userId) {
        projectFacade.validateProjectBelongsToTenant(projectId, tenantId);

        timeEntryRepository.findActiveByUserId(userId).ifPresent(active -> {
            throw new ActiveTimerConflictException();
        });

        TimeEntry entry = new TimeEntry();
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(Instant.now());
        entry.setDescription(description);

        return mapSingle(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryResponseDTO stop(UUID userId) {
        TimeEntry entry = timeEntryRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new TimeEntryNotFoundException("Nenhum timer ativo encontrado"));

        entry.setEndTime(Instant.now());
        return mapSingle(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryResponseDTO create(UUID projectId, String description, Instant startTime, Instant endTime,
            UUID tenantId, UUID userId) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidTimeBoundsException();
        }

        projectFacade.validateProjectBelongsToTenant(projectId, tenantId);

        TimeEntry entry = new TimeEntry();
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);
        entry.setDescription(description);

        return mapSingle(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryResponseDTO patch(UUID id, TimeEntryPatchRequestDTO request, UUID tenantId, UUID userId) {
        TimeEntry entry = findByIdAndUserId(id, userId);

        if (request.description() != null) {
            entry.setDescription(request.description());
        }
        if (request.startTime() != null) {
            entry.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            entry.setEndTime(request.endTime());
        }
        if (request.projectId() != null) {
            projectFacade.validateProjectBelongsToTenant(request.projectId(), tenantId);
            entry.setProjectId(request.projectId());
        }

        if (entry.getEndTime() != null && !entry.getEndTime().isAfter(entry.getStartTime())) {
            throw new InvalidTimeBoundsException();
        }

        return mapSingle(timeEntryRepository.save(entry));
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        TimeEntry entry = findByIdAndUserId(id, userId);
        timeEntryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public Optional<TimeEntryResponseDTO> findActive(UUID userId) {
        return timeEntryRepository.findActiveByUserId(userId).map(this::mapSingle);
    }

    @Transactional(readOnly = true)
    public Page<TimeEntryResponseDTO> findPageByUser(UUID userId, UUID projectId, Pageable pageable) {
        Page<TimeEntry> page = (projectId != null)
                ? timeEntryRepository.findByUserIdAndProjectId(userId, projectId, pageable)
                : timeEntryRepository.findByUserId(userId, pageable);

        Map<UUID, String> names = resolveNames(page.getContent());
        return page.map(e -> timeEntryMapper.toResponse(e, names));
    }

    private TimeEntry findByIdAndUserId(UUID id, UUID userId) {
        return timeEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(TimeEntryNotFoundException::new);
    }

    private TimeEntryResponseDTO mapSingle(TimeEntry entry) {
        Map<UUID, String> names = projectFacade.getProjectNames(Set.of(entry.getProjectId()));
        return timeEntryMapper.toResponse(entry, names);
    }

    private Map<UUID, String> resolveNames(List<TimeEntry> entries) {
        if (entries.isEmpty()) {
            return Map.of();
        }
        Set<UUID> ids = entries.stream()
                .map(TimeEntry::getProjectId)
                .collect(Collectors.toSet());
        return projectFacade.getProjectNames(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalTimeSecondsByProject(UUID projectId, UUID userId) {
        return timeEntryRepository.sumDurationSecondsByProjectIdAndUserId(projectId, userId);
    }
}
