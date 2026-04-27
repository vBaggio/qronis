package com.qronis.modules.tracker.api;

import com.qronis.modules.tracker.api.dto.TimeEntryCreateRequestDTO;
import com.qronis.modules.tracker.api.dto.TimeEntryPatchRequestDTO;
import com.qronis.modules.tracker.api.dto.TimeEntryResponseDTO;
import com.qronis.modules.tracker.api.dto.TimeEntryStartRequestDTO;
import com.qronis.modules.tracker.application.TimeEntryMapper;
import com.qronis.modules.tracker.application.TimeEntryService;
import com.qronis.modules.tracker.domain.entity.TimeEntry;

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

    private final TimeEntryService timeEntryService;
    private final TimeEntryMapper timeEntryMapper;

    public TimeEntryController(TimeEntryService timeEntryService, TimeEntryMapper timeEntryMapper) {
        this.timeEntryService = timeEntryService;
        this.timeEntryMapper = timeEntryMapper;
    }

    @PostMapping("/start")
    public ResponseEntity<TimeEntryResponseDTO> start(@Valid @RequestBody TimeEntryStartRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());
        TimeEntry entry = timeEntryService.start(request.projectId(), request.description(), tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryMapper.toResponse(entry));
    }

    @PutMapping("/stop")
    public ResponseEntity<TimeEntryResponseDTO> stop(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        TimeEntry entry = timeEntryService.stop(userId);
        return ResponseEntity.ok(timeEntryMapper.toResponse(entry));
    }

    @GetMapping("/active")
    public ResponseEntity<TimeEntryResponseDTO> active(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return timeEntryService.findActive(userId)
                .map(entry -> ResponseEntity.ok(timeEntryMapper.toResponse(entry)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Page<TimeEntryResponseDTO>> history(
            @RequestParam(name = "projectId", required = false) UUID projectId,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Page<TimeEntry> entries = timeEntryService.findByUserIdAndOptionalProjectId(userId, projectId, pageable);
        return ResponseEntity.ok(entries.map(timeEntryMapper::toResponse));
    }

    @PostMapping
    public ResponseEntity<TimeEntryResponseDTO> create(@Valid @RequestBody TimeEntryCreateRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());
        TimeEntry entry = timeEntryService.create(
                request.projectId(), request.description(),
                request.startTime(), request.endTime(),
                tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryMapper.toResponse(entry));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TimeEntryResponseDTO> patch(@PathVariable UUID id,
            @RequestBody TimeEntryPatchRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
        UUID userId = UUID.fromString(jwt.getSubject());
        TimeEntry entry = timeEntryService.patch(id, request, tenantId, userId);
        return ResponseEntity.ok(timeEntryMapper.toResponse(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        timeEntryService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
