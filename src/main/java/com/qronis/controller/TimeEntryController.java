package com.qronis.controller;

import com.qronis.dto.TimeEntryResponse;
import com.qronis.dto.TimeEntryStartRequest;
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
    public ResponseEntity<TimeEntryResponse> start(@Valid @RequestBody TimeEntryStartRequest request) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        TimeEntry entry = timeEntryService.start(
                request.projectId(), request.description(), auth.tenantId(), auth.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryMapper.toResponse(entry));
    }

    @PutMapping("/{id}/stop")
    public ResponseEntity<TimeEntryResponse> stop(@PathVariable UUID id) {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        TimeEntry entry = timeEntryService.stop(id, auth.userId());
        return ResponseEntity.ok(timeEntryMapper.toResponse(entry));
    }

    @GetMapping("/active")
    public ResponseEntity<TimeEntryResponse> active() {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        return timeEntryService.findActive(auth.userId())
                .map(entry -> ResponseEntity.ok(timeEntryMapper.toResponse(entry)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryResponse>> history() {
        AuthenticatedUser auth = AuthenticatedUser.fromContext();
        List<TimeEntry> entries = timeEntryService.findByUserId(auth.userId());
        return ResponseEntity.ok(timeEntryMapper.toResponseList(entries));
    }
}
