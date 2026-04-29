package com.qronis.modules.tracker.web;

import com.qronis.modules.tracker.web.dto.TimeEntryCreateRequestDTO;
import com.qronis.modules.tracker.web.dto.TimeEntryPatchRequestDTO;
import com.qronis.modules.tracker.web.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.web.dto.TimeEntryStartRequestDTO;
import com.qronis.modules.tracker.application.TrackerService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final TrackerService trackerService;

    public TimeEntryController(TrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @PostMapping("/start")
    public ResponseEntity<TimeEntryResponseDTO> start(@Valid @RequestBody TimeEntryStartRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trackerService.start(request.projectId(), request.description(), tenantId, userId));
    }

    @PutMapping("/stop")
    public ResponseEntity<TimeEntryResponseDTO> stop(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(trackerService.stop(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<TimeEntryResponseDTO> active(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return trackerService.findActive(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Page<TimeEntryResponseDTO>> history(
            @RequestParam(name = "projectId", required = false) UUID projectId,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(trackerService.findPageByUser(userId, projectId, pageable));
    }

    @PostMapping
    public ResponseEntity<TimeEntryResponseDTO> create(@Valid @RequestBody TimeEntryCreateRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(trackerService.create(
                request.projectId(), request.description(),
                request.startTime(), request.endTime(),
                tenantId, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TimeEntryResponseDTO> patch(@PathVariable UUID id,
            @RequestBody TimeEntryPatchRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(trackerService.patch(id, request, tenantId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        trackerService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
