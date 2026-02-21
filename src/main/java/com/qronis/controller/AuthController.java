package com.qronis.controller;

import com.qronis.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request.name(), request.email(), request.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    public record RegisterRequest(
            @NotBlank(message = "Nome é obrigatório")
            String name,

            @NotBlank(message = "Email é obrigatório")
            @Email(message = "Email inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Email é obrigatório")
            @Email(message = "Email inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            String password
    ) {}
}
