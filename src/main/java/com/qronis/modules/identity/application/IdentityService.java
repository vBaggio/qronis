package com.qronis.modules.identity.application;

import com.qronis.modules.identity.api.IdentityFacade;
import com.qronis.modules.identity.api.dto.IdentityProvisionResult;
import com.qronis.modules.identity.api.dto.TenantUserAuthDTO;
import com.qronis.modules.identity.domain.entity.Tenant;
import com.qronis.modules.identity.domain.entity.TenantUser;
import com.qronis.modules.identity.domain.entity.User;
import com.qronis.modules.identity.domain.enums.Role;
import com.qronis.modules.identity.api.exception.UserAlreadyExistsException;
import com.qronis.modules.identity.infrastructure.persistence.TenantRepository;
import com.qronis.modules.identity.infrastructure.persistence.TenantUserRepository;
import com.qronis.modules.identity.infrastructure.persistence.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IdentityService implements IdentityFacade {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;

    public IdentityService(UserRepository userRepository, TenantRepository tenantRepository, TenantUserRepository tenantUserRepository) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantUserRepository = tenantUserRepository;
    }

    @Override
    @Transactional
    public IdentityProvisionResult provisionTenant(String name, String email, String encodedPassword, String companyName) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        User user = new User(email, encodedPassword, name);
        user = userRepository.save(user);

        Tenant tenant = new Tenant(companyName);
        tenant = tenantRepository.save(tenant);

        TenantUser tenantUser = new TenantUser(tenant, user, Role.OWNER);
        tenantUserRepository.save(tenantUser);

        return new IdentityProvisionResult(user.getId(), user.getName(), user.getEmail(), tenant.getId(), Role.OWNER.name());
    }

    @Override
    public Optional<TenantUserAuthDTO> getAuthDetailsByEmail(String email) {
        return tenantUserRepository.findByUserEmailWithUser(email)
                .map(tu -> new TenantUserAuthDTO(
                        tu.getUser().getId(),
                        tu.getUser().getName(),
                        tu.getUser().getEmail(),
                        tu.getUser().getPassword(),
                        tu.getId().getTenantId(),
                        tu.getRole().name()
                ));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
