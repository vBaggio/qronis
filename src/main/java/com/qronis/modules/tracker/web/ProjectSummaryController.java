package com.qronis.modules.tracker.web;

import com.qronis.modules.tracker.application.TrackerService;
import com.qronis.modules.tracker.web.dto.ProjectSummaryResponseDTO;
import com.qronis.modules.tracker.web.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.api.TrackerFacade;
import com.qronis.modules.project.api.ProjectFacade;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectSummaryController {

    private final TrackerFacade trackerFacade;
    private final ProjectFacade projectFacade;
    private final TrackerService trackerService;

    public ProjectSummaryController(TrackerFacade trackerFacade,
                                    ProjectFacade projectFacade,
                                    TrackerService trackerService) {
        this.trackerFacade = trackerFacade;
        this.projectFacade = projectFacade;
        this.trackerService = trackerService;
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ProjectSummaryResponseDTO> getSummary(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());

        projectFacade.validateProjectBelongsToTenant(id, tenantId);
        Long totalSeconds = trackerFacade.getTotalTimeSecondsByProject(id, userId);

        return ResponseEntity.ok(new ProjectSummaryResponseDTO(id, totalSeconds != null ? totalSeconds : 0L));
    }

    @GetMapping("/{id}/time-entries")
    public ResponseEntity<List<TimeEntryResponseDTO>> listTimeEntries(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        return ResponseEntity.ok(trackerService.findByProjectId(id, tenantId));
    }
}
