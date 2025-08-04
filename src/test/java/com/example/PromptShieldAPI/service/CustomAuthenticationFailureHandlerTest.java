package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationFailureHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomAuthenticationFailureHandler handler;

    @BeforeEach
    void setUp() {
        // Configurar comportamento padrão do mock
        when(request.getParameter("email")).thenReturn("test@example.com");
    }

    @Test
    void shouldRedirectToDeletedErrorWhenAccountIsDeleted() throws Exception {
        // Arrange
        User deletedUser = new User();
        deletedUser.setEmail("test@example.com");
        deletedUser.softDelete("admin");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(deletedUser));
        when(request.getParameter("email")).thenReturn("test@example.com");

        // Act
        handler.onAuthenticationFailure(request, response, new BadCredentialsException("Bad credentials"));

        // Assert
        verify(response).sendRedirect("/auth/login?error=deleted");
    }

    @Test
    void shouldRedirectToDeletedErrorWhenUsernameNotFoundExceptionContainsDeletedMessage() throws Exception {
        // Arrange
        UsernameNotFoundException exception = new UsernameNotFoundException("Conta deletada. Não é possível fazer login.");

        // Act
        handler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect("/auth/login?error=deleted");
    }

    @Test
    void shouldRedirectToGenericErrorWhenAccountIsNotDeleted() throws Exception {
        // Arrange
        User activeUser = new User();
        activeUser.setEmail("test@example.com");
        activeUser.setActive(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(request.getParameter("email")).thenReturn("test@example.com");

        // Act
        handler.onAuthenticationFailure(request, response, new BadCredentialsException("Bad credentials"));

        // Assert
        verify(response).sendRedirect("/auth/login?error=true");
    }

    @Test
    void shouldRedirectToGenericErrorWhenUserNotFound() throws Exception {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(request.getParameter("email")).thenReturn("test@example.com");

        // Act
        handler.onAuthenticationFailure(request, response, new BadCredentialsException("Bad credentials"));

        // Assert
        verify(response).sendRedirect("/auth/login?error=true");
    }

    @Test
    void shouldRedirectToGenericErrorWhenEmailIsEmpty() throws Exception {
        // Arrange
        when(request.getParameter("email")).thenReturn("");

        // Act
        handler.onAuthenticationFailure(request, response, new BadCredentialsException("Bad credentials"));

        // Assert
        verify(response).sendRedirect("/auth/login?error=true");
    }

    @Test
    void shouldRedirectToGenericErrorWhenEmailIsNull() throws Exception {
        // Arrange
        when(request.getParameter("email")).thenReturn(null);

        // Act
        handler.onAuthenticationFailure(request, response, new BadCredentialsException("Bad credentials"));

        // Assert
        verify(response).sendRedirect("/auth/login?error=true");
    }
} 