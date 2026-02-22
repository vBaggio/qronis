package com.qronis.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    JwtEncoder jwtEncoder(JwtProperties properties) {
        SecretKey key = hmacKey(properties.getSecret());
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties properties) {
        SecretKey key = hmacKey(properties.getSecret());
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(jwtValidator(properties.getIssuer()));
        return decoder;
    }

    private SecretKey hmacKey(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private OAuth2TokenValidator<Jwt> jwtValidator(String issuer) {
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> claimsValidator = this::validateClaims;
        return new DelegatingOAuth2TokenValidator<>(defaultValidators, claimsValidator);
    }

    private OAuth2TokenValidatorResult validateClaims(Jwt jwt) {
        List<String> missing = new ArrayList<>();
        if (!hasTextClaim(jwt, "email"))
            missing.add("email");
        if (!hasTextClaim(jwt, "tenantId"))
            missing.add("tenantId");
        if (!hasTextClaim(jwt, "role"))
            missing.add("role");

        if (missing.isEmpty()) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                "invalid_token",
                "Missing required claims: " + String.join(", ", missing),
                null));
    }

    private boolean hasTextClaim(Jwt jwt, String claim) {
        String value = jwt.getClaimAsString(claim);
        return value != null && !value.isBlank();
    }
}
