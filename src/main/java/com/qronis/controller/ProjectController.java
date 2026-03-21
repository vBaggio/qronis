package com.qronis.controller;

import com.qronis.dto.ProjectRequestDTO;
import com.qronis.dto.ProjectResponseDTO;
import com.qronis.dto.ProjectSummaryResponseDTO;
import com.qronis.dto.TimeEntryResponseDTO;
import com.qronis.entity.Project;
import com.qronis.entity.TimeEntry;
import com.qronis.mapper.ProjectMapper;
import com.qronis.mapper.TimeEntryMapper;
import com.qronis.security.AuthenticatedUser;
import com.qronis.service.ProjectService;
import com.qronis.service.TimeEntryService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<Page<ProjectResponseDTO>> list(
            @RequestParam(name = "name", required = false) String name,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        Page<Project> projects = projectService.findByTenantId(auth.tenantId(), name, pageable);
        return ResponseEntity.ok(projects.map(projectMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getById(@PathVariable("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            AuthenticatedUser auth = AuthenticatedUser.fromContext();
            Project project = projectService.findByIdAndTenantId(uuid, auth.tenantId());
            return ResponseEntity.ok(projectMapper.toResponse(project));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Project ID format");
        }
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> create(@Valid @RequestBody ProjectRequestDTO request) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        Project project = projectService.create(request.name(), auth.tenantId(), auth.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMapper.toResponse(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> update(@PathVariable("id") String id,
            @Valid @RequestBody ProjectRequestDTO request) {
        try {
            UUID uuid = UUID.fromString(id);
            AuthenticatedUser auth = AuthenticatedUser.fromContext();
            Project project = projectService.update(uuid, auth.tenantId(), request.name());
            return ResponseEntity.ok(projectMapper.toResponse(project));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Project ID format");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            AuthenticatedUser auth = AuthenticatedUser.fromContext();
            projectService.delete(uuid, auth.tenantId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Project ID format");
        }
    }

    @GetMapping("/{id}/time-entries")
    public ResponseEntity<List<TimeEntryResponseDTO>> listTimeEntries(@PathVariable("id") UUID id) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        List<TimeEntry> entries = timeEntryService.findByProjectId(id, auth.tenantId());
        return ResponseEntity.ok(timeEntryMapper.toResponseList(entries));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ProjectSummaryResponseDTO> getSummary(@PathVariable("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            AuthenticatedUser auth = AuthenticatedUser.fromContext();
            ProjectSummaryResponseDTO summary = projectService.getProjectSummary(uuid, auth.tenantId(),
                    auth.userId());
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Project ID format");
        }
    }
}
