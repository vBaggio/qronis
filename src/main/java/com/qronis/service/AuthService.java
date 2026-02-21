package com.qronis.service;

import com.qronis.entity.Role;
import com.qronis.entity.Tenant;
import com.qronis.entity.TenantUser;
import com.qronis.entity.User;
import com.qronis.repository.TenantRepository;
import com.qronis.repository.TenantUserRepository;
import com.qronis.repository.UserRepository;
import com.qronis.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            TenantUserRepository tenantUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public String register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado: " + email);
        }

        // 1. Criar usuário
        User user = new User(email, passwordEncoder.encode(password), name);
        user = userRepository.save(user);

        // 2. Criar tenant automaticamente
        Tenant tenant = new Tenant(name);
        tenant = tenantRepository.save(tenant);

        // 3. Associar usuário ao tenant como OWNER
        TenantUser tenantUser = new TenantUser(tenant, user, Role.OWNER);
        tenantUserRepository.save(tenantUser);

        // 4. Gerar e retornar JWT
        return jwtService.generateToken(user);
    }

    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return jwtService.generateToken(user);
    }
}
