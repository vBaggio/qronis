package com.qronis.modules.identity.api;

import com.qronis.modules.identity.api.dto.IdentityProvisionResult;
import com.qronis.modules.identity.api.dto.TenantUserAuthDTO;
import org.springframework.modulith.NamedInterface;

import java.util.Optional;

@NamedInterface("api")
public interface IdentityFacade {
    IdentityProvisionResult provisionTenant(String name, String email, String encodedPassword, String companyName);
    Optional<TenantUserAuthDTO> getAuthDetailsByEmail(String email);
    boolean existsByEmail(String email);
}
