package com.qronis.modules.auth.application;

import com.qronis.modules.auth.domain.exception.InvalidCredentialsException;
import com.qronis.modules.identity.api.IdentityFacade;
import com.qronis.modules.identity.api.dto.IdentityProvisionResult;
import com.qronis.modules.identity.api.dto.TenantUserAuthDTO;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final IdentityFacade identityFacade;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            IdentityFacade identityFacade,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.identityFacade = identityFacade;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String register(String name, String email, String password, String companyName) {
        IdentityProvisionResult result = identityFacade.provisionTenant(
                name, email, passwordEncoder.encode(password), companyName);

        return jwtService.generateToken(result.userId(), result.name(), result.email(), result.tenantId(), result.role());
    }

    public String login(String email, String password) {
        TenantUserAuthDTO authDetails = identityFacade.getAuthDetailsByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, authDetails.encodedPassword())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(authDetails.userId(), authDetails.name(), authDetails.email(), authDetails.tenantId(), authDetails.role());
    }
}
