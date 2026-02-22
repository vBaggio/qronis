package com.qronis.service;

import com.qronis.entity.Project;
import com.qronis.entity.Tenant;
import com.qronis.entity.User;
import com.qronis.exception.ResourceNotFoundException;
import com.qronis.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
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
}
