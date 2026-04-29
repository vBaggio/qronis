package com.qronis.modules.project.infrastructure.persistence;

import com.qronis.modules.project.domain.entity.Project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query(value = """
            SELECT p FROM Project p
            WHERE p.tenantId = :tenantId
            AND LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, p.name), '%'))
            """, countQuery = """
            SELECT COUNT(p) FROM Project p
            WHERE p.tenantId = :tenantId
            AND LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:name, p.name), '%'))
            """)
    Page<Project> findByTenantId(@Param("tenantId") UUID tenantId, @Param("name") String name,
            Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC")
    List<Project> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<Project> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
