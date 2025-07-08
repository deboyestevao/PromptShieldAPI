package com.example.SafeMindAPI.controller;

import com.example.SafeMindAPI.dto.LoginRequest;
import com.example.SafeMindAPI.dto.RegisterRequest;
import com.example.SafeMindAPI.model.User;
import com.example.SafeMindAPI.repository.UserRepository;
import com.example.SafeMindAPI.service.AuthService;
import com.example.SafeMindAPI.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SystemConfigService systemConfigService;
    private final AuthService authService;


    @PostMapping("/login")
    @Operation(summary = "User login", security = @SecurityRequirement(name = ""))
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        authService.login(request, session);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    @Operation(summary = "User register", security = @SecurityRequirement(name = ""))
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        authService.delete(id);
    }

}
