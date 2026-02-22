package com.qronis.service;

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
        // Valida que o projeto pertence ao tenant do usuário
        Project project = projectService.findByIdAndTenantId(projectId, tenantId);

        // Verifica se já existe timer ativo (mensagem amigável antes do constraint do
        // banco)
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
    public TimeEntry stop(UUID id, UUID userId) {
        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado"));

        if (!entry.getCreatedBy().getId().equals(userId)) {
            throw new ResourceNotFoundException("Lançamento não encontrado");
        }

        if (!entry.isActive()) {
            throw new BusinessException("Este timer já foi finalizado");
        }

        entry.setEndTime(Instant.now());
        return timeEntryRepository.save(entry);
    }

    public Optional<TimeEntry> findActive(UUID userId) {
        return timeEntryRepository.findActiveByUserId(userId);
    }

    public List<TimeEntry> findByUserId(UUID userId) {
        return timeEntryRepository.findByUserIdWithProject(userId);
    }
}
