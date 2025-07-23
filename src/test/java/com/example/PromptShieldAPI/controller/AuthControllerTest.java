package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.LoginRequest;
import com.example.PromptShieldAPI.dto.RegisterRequest;
import com.example.PromptShieldAPI.service.AuthService;
import com.example.PromptShieldAPI.service.SystemConfigService;
import com.example.PromptShieldAPI.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private SystemConfigService configService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin() {
        LoginRequest request = new LoginRequest();
        authController.login(request, session);
        verify(authService, times(1)).login(request, session);
    }

    @Test
    void testRegister() {
        RegisterRequest request = new RegisterRequest();
        ResponseEntity<?> response = authController.register(request);
        verify(authService).register(request);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDelete() {
        Long id = 1L;
        authController.delete(id);
        verify(authService).delete(id);
    }
}
