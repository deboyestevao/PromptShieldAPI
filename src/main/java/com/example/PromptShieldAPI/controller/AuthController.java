package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.LoginRequest;
import com.example.PromptShieldAPI.dto.RegisterRequest;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.service.AuthService;
import com.example.PromptShieldAPI.service.SystemConfigService;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        authService.login(request, session);
        return ResponseEntity.ok("Login efetuado com sucesso");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Registo concluído");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        authService.delete(id);
        return ResponseEntity.ok("Utilizador removido");
    }
}
