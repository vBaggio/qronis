package com.qronis.service;

import com.qronis.config.JwtProperties;
import com.qronis.entity.TenantUser;
import com.qronis.entity.User;
import com.qronis.repository.TenantUserRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final TenantUserRepository tenantUserRepository;
    private final JwtProperties jwtProperties;

    public JwtService(
            JwtEncoder jwtEncoder,
            TenantUserRepository tenantUserRepository,
            JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.tenantUserRepository = tenantUserRepository;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        TenantUser tenantUser = tenantUserRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário sem tenant associado"));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.getExpirationHours(), ChronoUnit.HOURS))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("tenantId", tenantUser.getTenant().getId().toString())
                .claim("role", tenantUser.getRole().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
