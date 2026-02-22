package com.qronis.repository;

import com.qronis.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    @Query("SELECT te FROM TimeEntry te JOIN FETCH te.project WHERE te.createdBy.id = :userId AND te.endTime IS NULL")
    Optional<TimeEntry> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT te FROM TimeEntry te JOIN FETCH te.project WHERE te.createdBy.id = :userId ORDER BY te.startTime DESC")
    List<TimeEntry> findByUserIdWithProject(@Param("userId") UUID userId);

    @Query("SELECT te FROM TimeEntry te JOIN FETCH te.project JOIN FETCH te.createdBy WHERE te.project.id = :projectId ORDER BY te.startTime DESC")
    List<TimeEntry> findByProjectIdWithProject(@Param("projectId") UUID projectId);
}
