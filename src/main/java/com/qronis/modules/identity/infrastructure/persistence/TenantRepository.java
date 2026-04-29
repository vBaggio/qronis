package com.qronis.modules.identity.infrastructure.persistence;

import com.qronis.modules.identity.domain.entity.Tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
