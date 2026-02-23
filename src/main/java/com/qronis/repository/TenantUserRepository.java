package com.qronis.repository;

import com.qronis.entity.TenantUser;
import com.qronis.entity.TenantUserId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TenantUserRepository extends JpaRepository<TenantUser, TenantUserId> {

    Optional<TenantUser> findByUserId(UUID userId);

    @Query("SELECT tu FROM TenantUser tu JOIN FETCH tu.user JOIN FETCH tu.tenant WHERE tu.user.email = :email")
    Optional<TenantUser> findByUserEmailWithUser(@Param("email") String email);

    @Query("SELECT tu FROM TenantUser tu JOIN FETCH tu.user JOIN FETCH tu.tenant WHERE tu.user.id = :userId")
    Optional<TenantUser> findByUserIdWithUserAndTenant(@Param("userId") UUID userId);
}
