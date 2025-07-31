package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.LoginRequest;
import com.example.PromptShieldAPI.dto.RegisterRequest;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.service.AuthService;
import com.example.PromptShieldAPI.service.SystemConfigService;
import com.example.PromptShieldAPI.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ActivityLogService activityLogService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        authService.login(request, session);
        
        // Registrar atividade de login no log
        activityLogService.logActivity(
            "USER_LOGIN",
            "Login de Utilizador",
            "Utilizador '" + request.getUsername() + "' fez login no sistema",
            request.getUsername()
        );
        
        return ResponseEntity.ok("Login efetuado com sucesso");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        ResponseEntity<?> response = authService.register(request);
        
        // Se o registo foi bem-sucedido, registrar no log
        if (response.getStatusCode().is2xxSuccessful()) {
            activityLogService.logActivity(
                "USER_REGISTER",
                "Novo Utilizador",
                "Novo utilizador com email '" + request.getEmail() + "' registado no sistema",
                request.getEmail()
            );
        }
        
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        authService.delete(id);
        return ResponseEntity.ok("Utilizador removido");
    }
}
