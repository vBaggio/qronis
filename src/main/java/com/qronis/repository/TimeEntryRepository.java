package com.qronis.repository;

import com.qronis.entity.TimeEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    @Query("SELECT te FROM TimeEntry te JOIN FETCH te.project WHERE te.createdBy.id = :userId AND te.endTime IS NULL")
    Optional<TimeEntry> findActiveByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT te FROM TimeEntry te JOIN FETCH te.project WHERE te.createdBy.id = :userId", countQuery = "SELECT count(te) FROM TimeEntry te WHERE te.createdBy.id = :userId")
    Page<TimeEntry> findByUserIdWithProject(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = "SELECT te FROM TimeEntry te JOIN FETCH te.project WHERE te.createdBy.id = :userId AND te.project.id = :projectId", countQuery = "SELECT count(te) FROM TimeEntry te WHERE te.createdBy.id = :userId AND te.project.id = :projectId")
    Page<TimeEntry> findByUserIdAndProjectIdWithProject(@Param("userId") UUID userId,
            @Param("projectId") UUID projectId, Pageable pageable);

    @Query("SELECT te FROM TimeEntry te JOIN FETCH te.project JOIN FETCH te.createdBy WHERE te.project.id = :projectId ORDER BY te.startTime DESC")
    List<TimeEntry> findByProjectIdWithProject(@Param("projectId") UUID projectId);

    @Query("SELECT te FROM TimeEntry te JOIN FETCH te.project WHERE te.id = :id AND te.createdBy.id = :userId")
    Optional<TimeEntry> findByIdAndCreatedByIdWithProject(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query(value = "SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (te.end_time - te.start_time))), 0) FROM time_entry te WHERE te.project_id = CAST(:projectId AS uuid) AND te.created_by = CAST(:userId AS uuid) AND te.end_time IS NOT NULL", nativeQuery = true)
    Long sumDurationSecondsByProjectIdAndUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
