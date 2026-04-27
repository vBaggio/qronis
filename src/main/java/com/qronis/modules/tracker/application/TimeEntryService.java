package com.qronis.modules.tracker.application;

import com.qronis.modules.tracker.api.dto.TimeEntryPatchRequestDTO;
import com.qronis.modules.tracker.domain.entity.TimeEntry;
import com.qronis.modules.tracker.domain.exception.ActiveTimerConflictException;
import com.qronis.modules.tracker.domain.exception.InvalidTimeBoundsException;
import com.qronis.modules.tracker.domain.exception.TimeEntryNotFoundException;
import com.qronis.modules.tracker.application.repositories.TimeEntryRepository;
import com.qronis.modules.project.domain.entity.Project;
import com.qronis.modules.project.application.ProjectService;
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
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectService projectService;

    public TimeEntryService(TimeEntryRepository timeEntryRepository, ProjectService projectService) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectService = projectService;
    }

    @Transactional
    public TimeEntry start(UUID projectId, String description, UUID tenantId, UUID userId) {
        Project project = projectService.findByIdAndTenantId(projectId, tenantId);

        timeEntryRepository.findActiveByUserId(userId).ifPresent(active -> {
            throw new ActiveTimerConflictException();
        });

        User user = new User();
        user.setId(userId);

        TimeEntry entry = new TimeEntry();
        entry.setProject(project);
        entry.setCreatedBy(user);
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

        Project project = projectService.findByIdAndTenantId(projectId, tenantId);

        User user = new User();
        user.setId(userId);

        TimeEntry entry = new TimeEntry();
        entry.setProject(project);
        entry.setCreatedBy(user);
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
            Project project = projectService.findByIdAndTenantId(request.projectId(), tenantId);
            entry.setProject(project);
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
        projectService.findByIdAndTenantId(projectId, tenantId);
        return timeEntryRepository.findByProjectIdWithProject(projectId);
    }

    private TimeEntry findByIdAndUserId(UUID id, UUID userId) {
        return timeEntryRepository.findByIdAndCreatedByIdWithProject(id, userId)
                .orElseThrow(TimeEntryNotFoundException::new);
    }
}
