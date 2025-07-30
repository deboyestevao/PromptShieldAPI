package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.dto.LoginRequest;
import com.example.PromptShieldAPI.dto.RegisterRequest;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Login com sucesso
    @Test
    void shouldLoginSuccessfully() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("pass123");
        loginRequest.setUsername("user123");

        Authentication authMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);

        ResponseEntity<?> response = authService.login(loginRequest, session);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Welcome"));
    }

    // ❌ Login com falha (credenciais inválidas)
    @Test
    void shouldFailLoginWithInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid") {});

        ResponseEntity<?> response = authService.login(loginRequest, session);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid credentials.", response.getBody());
    }

    // ✅ Registro bem-sucedido
    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("João");
        registerRequest.setLastName("Silva");
        registerRequest.setPassword("pass123");
        registerRequest.setConfirmPassword("pass123");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("hashedPassword");

        ResponseEntity<?> response = authService.register(registerRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(userRepository).save(any(User.class));
    }

    // ❌ Registro com usuário já existente
    @Test
    void shouldFailRegisterWhenUserExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@example.com");
        registerRequest.setFirstName("João");
        registerRequest.setLastName("Silva");
        registerRequest.setPassword("pass123");
        registerRequest.setConfirmPassword("pass123");

        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User()));

        ResponseEntity<?> response = authService.register(registerRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    // ❌ Remover usuário inexistente
    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        doThrow(new org.springframework.dao.EmptyResultDataAccessException(1))
                .when(userRepository).deleteById(99L);

        Exception exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> authService.delete(99L)
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    // ✅ Remover usuário com sucesso
    @Test
    void shouldDeleteUserSuccessfully() {
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> authService.delete(1L));
    }
}
