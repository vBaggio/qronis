package com.qronis.repository;

import com.qronis.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    Optional<TimeEntry> findByCreatedByIdAndEndTimeIsNull(UUID userId);

    List<TimeEntry> findByCreatedByIdOrderByStartTimeDesc(UUID userId);

    List<TimeEntry> findByProjectIdOrderByStartTimeDesc(UUID projectId);
}
