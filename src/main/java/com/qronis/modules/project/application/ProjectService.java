package com.qronis.modules.project.application;

import com.qronis.modules.project.domain.entity.Project;
import com.qronis.modules.project.api.exception.ProjectNotFoundException;
import com.qronis.modules.project.api.ProjectFacade;
import com.qronis.modules.project.infrastructure.persistence.ProjectRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService implements ProjectFacade {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Page<Project> findByTenantId(UUID tenantId, String name, Pageable pageable) {
        return projectRepository.findByTenantId(tenantId, name, pageable);
    }

    public List<Project> findByTenantId(UUID tenantId) {
        return projectRepository.findByTenantId(tenantId);
    }

    public Project findByIdAndTenantId(UUID id, UUID tenantId) {
        return projectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ProjectNotFoundException(id.toString()));
    }

    @Transactional
    public Project create(String name, UUID tenantId, UUID userId) {
        Project project = new Project(name, tenantId, userId);
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

    @Override
    public void validateProjectBelongsToTenant(UUID projectId, UUID tenantId) {
        findByIdAndTenantId(projectId, tenantId);
    }

    @Override
    public String getProjectName(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(Project::getName)
                .orElse(null);
    }

    @Override
    public Map<UUID, String> getProjectNames(Set<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }
        return projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Project::getName));
    }
}
