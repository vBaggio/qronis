package com.qronis.service;

import com.qronis.dto.TimeEntryPatchRequestDTO;
import com.qronis.entity.Project;
import com.qronis.entity.TimeEntry;
import com.qronis.entity.User;
import com.qronis.exception.BusinessException;
import com.qronis.exception.ResourceNotFoundException;
import com.qronis.repository.TimeEntryRepository;
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
            throw new BusinessException("Já existe um timer ativo. Pare o timer atual antes de iniciar outro.");
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
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum timer ativo encontrado"));

        entry.setEndTime(Instant.now());
        return timeEntryRepository.save(entry);
    }

    @Transactional
    public TimeEntry create(UUID projectId, String description, Instant startTime, Instant endTime,
            UUID tenantId, UUID userId) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("Horário de término deve ser posterior ao de início");
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

        // Validar consistência de horários após todas as alterações
        if (entry.getEndTime() != null && !entry.getEndTime().isAfter(entry.getStartTime())) {
            throw new BusinessException("Horário de término deve ser posterior ao de início");
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

    public List<TimeEntry> findByUserId(UUID userId) {
        return timeEntryRepository.findByUserIdWithProject(userId);
    }

    public List<TimeEntry> findByProjectId(UUID projectId, UUID tenantId) {
        // Valida que o projeto pertence ao tenant
        projectService.findByIdAndTenantId(projectId, tenantId);
        return timeEntryRepository.findByProjectIdWithProject(projectId);
    }

    private TimeEntry findByIdAndUserId(UUID id, UUID userId) {
        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado"));

        if (!entry.getCreatedBy().getId().equals(userId)) {
            throw new ResourceNotFoundException("Lançamento não encontrado");
        }

        return entry;
    }
}
