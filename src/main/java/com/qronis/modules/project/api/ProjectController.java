package com.qronis.modules.project.api;

import com.qronis.modules.project.api.dto.ProjectRequestDTO;
import com.qronis.modules.project.api.dto.ProjectResponseDTO;
import com.qronis.modules.project.api.dto.ProjectSummaryResponseDTO;
import com.qronis.modules.project.application.ProjectMapper;
import com.qronis.modules.project.application.ProjectService;
import com.qronis.modules.project.domain.entity.Project;
import com.qronis.modules.tracker.api.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.application.TimeEntryMapper;
import com.qronis.modules.tracker.application.TimeEntryService;
import com.qronis.modules.tracker.domain.entity.TimeEntry;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<Page<ProjectResponseDTO>> list(
            @RequestParam(name = "name", required = false) String name,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        Page<Project> projects = projectService.findByTenantId(tenantId, name, pageable);
        return ResponseEntity.ok(projects.map(projectMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getById(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        Project project = projectService.findByIdAndTenantId(id, tenantId);
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> create(@Valid @RequestBody ProjectRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());
        Project project = projectService.create(request.name(), tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMapper.toResponse(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody ProjectRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        Project project = projectService.update(id, tenantId, request.name());
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        projectService.delete(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/time-entries")
    public ResponseEntity<List<TimeEntryResponseDTO>> listTimeEntries(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        List<TimeEntry> entries = timeEntryService.findByProjectId(id, tenantId);
        return ResponseEntity.ok(timeEntryMapper.toResponseList(entries));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ProjectSummaryResponseDTO> getSummary(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());
        ProjectSummaryResponseDTO summary = projectService.getProjectSummary(id, tenantId, userId);
        return ResponseEntity.ok(summary);
    }
}
