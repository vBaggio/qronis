package com.qronis.controller;

import com.qronis.dto.TimeEntryCreateRequestDTO;
import com.qronis.dto.TimeEntryPatchRequestDTO;
import com.qronis.dto.TimeEntryResponseDTO;
import com.qronis.dto.TimeEntryStartRequestDTO;
import com.qronis.entity.TimeEntry;
import com.qronis.mapper.TimeEntryMapper;
import com.qronis.security.AuthenticatedUser;
import com.qronis.service.TimeEntryService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<TimeEntryResponseDTO> start(@Valid @RequestBody TimeEntryStartRequestDTO request) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        TimeEntry entry = timeEntryService.start(
                request.projectId(), request.description(), auth.tenantId(), auth.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryMapper.toResponse(entry));
    }

    @PutMapping("/stop")
    public ResponseEntity<TimeEntryResponseDTO> stop() {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        TimeEntry entry = timeEntryService.stop(auth.userId());
        return ResponseEntity.ok(timeEntryMapper.toResponse(entry));
    }

    @GetMapping("/active")
    public ResponseEntity<TimeEntryResponseDTO> active() {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        return timeEntryService.findActive(auth.userId())
                .map(entry -> ResponseEntity.ok(timeEntryMapper.toResponse(entry)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryResponseDTO>> history() {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        List<TimeEntry> entries = timeEntryService.findByUserId(auth.userId());
        return ResponseEntity.ok(timeEntryMapper.toResponseList(entries));
    }

    @PostMapping
    public ResponseEntity<TimeEntryResponseDTO> create(@Valid @RequestBody TimeEntryCreateRequestDTO request) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        TimeEntry entry = timeEntryService.create(
                request.projectId(), request.description(),
                request.startTime(), request.endTime(),
                auth.tenantId(), auth.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryMapper.toResponse(entry));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TimeEntryResponseDTO> patch(@PathVariable("id") UUID id,
            @RequestBody TimeEntryPatchRequestDTO request) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        TimeEntry entry = timeEntryService.patch(id, request, auth.tenantId(), auth.userId());
        return ResponseEntity.ok(timeEntryMapper.toResponse(entry));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        timeEntryService.delete(id, auth.userId());
        return ResponseEntity.noContent().build();
    }
}
