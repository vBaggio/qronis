package com.qronis.controller;

import com.qronis.dto.ProjectRequest;
import com.qronis.dto.ProjectResponse;
import com.qronis.entity.Project;
import com.qronis.mapper.ProjectMapper;
import com.qronis.security.AuthenticatedUser;
import com.qronis.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectController(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list() {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        List<Project> projects = projectService.findByTenantId(auth.tenantId());
        return ResponseEntity.ok(projectMapper.toResponseList(projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable UUID id) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        Project project = projectService.findByIdAndTenantId(id, auth.tenantId());
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        Project project = projectService.create(request.name(), auth.tenantId(), auth.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMapper.toResponse(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        Project project = projectService.update(id, auth.tenantId(), request.name());
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        projectService.delete(id, auth.tenantId());
        return ResponseEntity.noContent().build();
    }
}
