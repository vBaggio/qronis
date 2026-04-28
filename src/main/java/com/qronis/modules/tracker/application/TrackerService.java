package com.qronis.modules.tracker.application;

import com.qronis.modules.tracker.web.dto.TimeEntryPatchRequestDTO;
import com.qronis.modules.tracker.domain.entity.TimeEntry;
import com.qronis.modules.tracker.domain.exception.ActiveTimerConflictException;
import com.qronis.modules.tracker.domain.exception.InvalidTimeBoundsException;
import com.qronis.modules.tracker.domain.exception.TimeEntryNotFoundException;
import com.qronis.modules.tracker.infrastructure.persistence.TimeEntryRepository;
import com.qronis.modules.tracker.api.TrackerFacade;
import com.qronis.modules.project.api.ProjectFacade;
import com.qronis.modules.identity.domain.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TrackerService implements TrackerFacade {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectFacade projectFacade;

    public TrackerService(TimeEntryRepository timeEntryRepository, ProjectFacade projectFacade) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectFacade = projectFacade;
    }

    @Transactional
    public TimeEntry start(UUID projectId, String description, UUID tenantId, UUID userId) {
        projectFacade.validateProjectBelongsToTenant(projectId, tenantId);

        timeEntryRepository.findActiveByUserId(userId).ifPresent(active -> {
            throw new ActiveTimerConflictException();
        });

        TimeEntry entry = new TimeEntry();
        entry.setProjectId(projectId);
        entry.setUserId(userId);
        entry.setStartTime(Instant.now());
        entry.setDescription(description);

        return timeEntryRepository.save(entry);
    }

    @Transactional
    public TimeEntry stop(UUID userId) {
        TimeEntry entry = timeEntryRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new TimeEntryNotFoundException("Nenhum timer ativo encontrado"));

        entry.setEndTime(Instant.now());
        return timeEntryRepository.save(entry);
    }

    @Transactional
    public TimeEntry create(UUID projectId, String description, Instant startTime, Instant endTime,
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

        return timeEntryRepository.save(entry);
    }

    @Transactional
    public TimeEntry patch(UUID id, TimeEntryPatchRequestDTO request, UUID tenantId, UUID userId) {
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
            entry.setProjectId(request.projectId());
        }

        if (entry.getEndTime() != null && !entry.getEndTime().isAfter(entry.getStartTime())) {
            throw new InvalidTimeBoundsException();
        }

        return timeEntryRepository.save(entry);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        TimeEntry entry = findByIdAndUserId(id, userId);
        timeEntryRepository.delete(entry);
    }

    public Optional<TimeEntry> findActive(UUID userId) {
        return timeEntryRepository.findActiveByUserId(userId);
    }

    public Page<TimeEntry> findByUserIdAndOptionalProjectId(UUID userId, UUID projectId, Pageable pageable) {
        if (projectId != null) {
            return timeEntryRepository.findByUserIdAndProjectIdWithProject(userId, projectId, pageable);
        }
        return timeEntryRepository.findByUserIdWithProject(userId, pageable);
    }

    public List<TimeEntry> findByProjectId(UUID projectId, UUID tenantId) {
        projectFacade.validateProjectBelongsToTenant(projectId, tenantId);
        return timeEntryRepository.findByProjectIdWithProject(projectId);
    }

    private TimeEntry findByIdAndUserId(UUID id, UUID userId) {
        return timeEntryRepository.findByIdAndCreatedByIdWithProject(id, userId)
                .orElseThrow(TimeEntryNotFoundException::new);
    }

    @Override
    public Long getTotalTimeSecondsByProject(UUID projectId, UUID userId) {
        return timeEntryRepository.sumDurationSecondsByProjectIdAndUserId(projectId, userId);
    }
}
