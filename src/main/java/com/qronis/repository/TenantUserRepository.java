package com.qronis.repository;

import com.qronis.entity.TenantUser;
import com.qronis.entity.TenantUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantUserRepository extends JpaRepository<TenantUser, TenantUserId> {

    Optional<TenantUser> findByUserId(UUID userId);
}
