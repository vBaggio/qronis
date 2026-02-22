package com.qronis.repository;

import com.qronis.entity.Project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("SELECT p FROM Project p JOIN FETCH p.createdBy WHERE p.tenant.id = :tenantId ORDER BY p.createdAt DESC")
    List<Project> findByTenantIdWithCreator(@Param("tenantId") UUID tenantId);

    @Query("SELECT p FROM Project p JOIN FETCH p.createdBy WHERE p.id = :id AND p.tenant.id = :tenantId")
    Optional<Project> findByIdAndTenantIdWithCreator(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
