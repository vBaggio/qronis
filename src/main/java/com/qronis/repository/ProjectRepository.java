package com.qronis.repository;

import com.qronis.entity.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query(value = "SELECT p FROM Project p JOIN FETCH p.createdBy WHERE p.tenant.id = :tenantId", countQuery = "SELECT COUNT(p) FROM Project p WHERE p.tenant.id = :tenantId")
    Page<Project> findByTenantIdWithCreator(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT p FROM Project p JOIN FETCH p.createdBy WHERE p.tenant.id = :tenantId ORDER BY p.createdAt DESC")
    List<Project> findByTenantIdWithCreator(@Param("tenantId") UUID tenantId);

    @Query("SELECT p FROM Project p JOIN FETCH p.createdBy WHERE p.id = :id AND p.tenant.id = :tenantId")
    Optional<Project> findByIdAndTenantIdWithCreator(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
