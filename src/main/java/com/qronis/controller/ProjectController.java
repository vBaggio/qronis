package com.qronis.controller;

import com.qronis.dto.ProjectRequestDTO;
import com.qronis.dto.ProjectResponseDTO;
import com.qronis.dto.TimeEntryResponseDTO;
import com.qronis.entity.Project;
import com.qronis.entity.TimeEntry;
import com.qronis.mapper.ProjectMapper;
import com.qronis.mapper.TimeEntryMapper;
import com.qronis.security.AuthenticatedUser;
import com.qronis.service.ProjectService;
import com.qronis.service.TimeEntryService;

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
    private final TimeEntryService timeEntryService;
    private final TimeEntryMapper timeEntryMapper;

    public ProjectController(ProjectService projectService, ProjectMapper projectMapper,
            TimeEntryService timeEntryService, TimeEntryMapper timeEntryMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
        this.timeEntryService = timeEntryService;
        this.timeEntryMapper = timeEntryMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> list() {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        List<Project> projects = projectService.findByTenantId(auth.tenantId());
        return ResponseEntity.ok(projectMapper.toResponseList(projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getById(@PathVariable UUID id) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        Project project = projectService.findByIdAndTenantId(id, auth.tenantId());
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> create(@Valid @RequestBody ProjectRequestDTO request) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        Project project = projectService.create(request.name(), auth.tenantId(), auth.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMapper.toResponse(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody ProjectRequestDTO request) {
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

    @GetMapping("/{id}/time-entries")
    public ResponseEntity<List<TimeEntryResponseDTO>> listTimeEntries(@PathVariable UUID id) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        List<TimeEntry> entries = timeEntryService.findByProjectId(id, auth.tenantId());
        return ResponseEntity.ok(timeEntryMapper.toResponseList(entries));
    }
}
