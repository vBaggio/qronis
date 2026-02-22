package com.qronis.controller;

import com.qronis.dto.AuthResponseDTO;
import com.qronis.dto.LoginRequestDTO;
import com.qronis.dto.RegisterRequestDTO;
import com.qronis.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        String token = authService.register(request.name(), request.email(), request.password(), request.companyName());
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }
}
