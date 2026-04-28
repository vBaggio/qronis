package com.qronis.modules.project.application;

import com.qronis.modules.project.domain.entity.Project;
import com.qronis.modules.project.domain.exception.ProjectNotFoundException;
import com.qronis.modules.project.api.ProjectFacade;
import com.qronis.modules.project.infrastructure.persistence.ProjectRepository;
import com.qronis.modules.identity.domain.entity.Tenant;
import com.qronis.modules.identity.domain.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService implements ProjectFacade {

    private final ProjectRepository projectRepository;
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Page<Project> findByTenantId(UUID tenantId, String name, Pageable pageable) {
        return projectRepository.findByTenantIdWithCreator(tenantId, name, pageable);
    }

    public List<Project> findByTenantId(UUID tenantId) {
        return projectRepository.findByTenantIdWithCreator(tenantId);
    }

    public Project findByIdAndTenantId(UUID id, UUID tenantId) {
        return projectRepository.findByIdAndTenantIdWithCreator(id, tenantId)
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
}
