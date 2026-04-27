package com.qronis.modules.auth.application;

import com.qronis.modules.auth.domain.exception.InvalidCredentialsException;
import com.qronis.modules.identity.domain.enums.Role;
import com.qronis.modules.identity.domain.entity.Tenant;
import com.qronis.modules.identity.domain.entity.TenantUser;
import com.qronis.modules.identity.domain.entity.User;
import com.qronis.modules.identity.application.repositories.TenantRepository;
import com.qronis.modules.identity.application.repositories.TenantUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserService userService,
            TenantRepository tenantRepository,
            TenantUserRepository tenantUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userService = userService;
        this.tenantRepository = tenantRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String register(String name, String email, String password, String companyName) {
        if (userService.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado: " + email);
        }

        User user = new User(email, passwordEncoder.encode(password), name);
        user = userService.save(user);

        Tenant tenant = new Tenant(companyName);
        tenant = tenantRepository.save(tenant);

        TenantUser tenantUser = new TenantUser(tenant, user, Role.OWNER);
        tenantUserRepository.save(tenantUser);

        return jwtService.generateToken(user, tenant.getId(), Role.OWNER);
    }

    public String login(String email, String password) {
        TenantUser tenantUser = tenantUserRepository.findByUserEmailWithUser(email)
                .orElseThrow(InvalidCredentialsException::new);

        User user = tenantUser.getUser();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(user, tenantUser.getId().getTenantId(), tenantUser.getRole());
    }
}
