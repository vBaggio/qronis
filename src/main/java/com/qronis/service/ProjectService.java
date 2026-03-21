package com.qronis.service;

import com.qronis.dto.ProjectSummaryResponseDTO;
import com.qronis.entity.Project;
import com.qronis.entity.Tenant;
import com.qronis.entity.User;
import com.qronis.exception.ResourceNotFoundException;
import com.qronis.repository.ProjectRepository;
import com.qronis.repository.TimeEntryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;

    public ProjectService(ProjectRepository projectRepository, TimeEntryRepository timeEntryRepository) {
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    public Page<Project> findByTenantId(UUID tenantId, String name, Pageable pageable) {
        return projectRepository.findByTenantIdWithCreator(tenantId, name, pageable);
    }

    public List<Project> findByTenantId(UUID tenantId) {
        return projectRepository.findByTenantIdWithCreator(tenantId);
    }

    public Project findByIdAndTenantId(UUID id, UUID tenantId) {
        return projectRepository.findByIdAndTenantIdWithCreator(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
    }

    @Transactional
    public Project create(String name, UUID tenantId, UUID userId) {
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);

        User user = new User();
        user.setId(userId);

        Project project = new Project(name, tenant, user);
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(UUID id, UUID tenantId, String name) {
        Project project = findByIdAndTenantId(id, tenantId);
        project.setName(name);
        return projectRepository.save(project);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId) {
        Project project = findByIdAndTenantId(id, tenantId);
        projectRepository.delete(project);
    }

    public ProjectSummaryResponseDTO getProjectSummary(UUID projectId, UUID tenantId, UUID userId) {
        // Validation check for tenant ownership
        findByIdAndTenantId(projectId, tenantId);
        Long totalSeconds = timeEntryRepository.sumDurationSecondsByProjectIdAndUserId(projectId, userId);
        return new ProjectSummaryResponseDTO(projectId, totalSeconds != null ? totalSeconds : 0L);
    }
}
