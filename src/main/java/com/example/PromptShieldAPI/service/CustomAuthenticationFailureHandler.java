package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        String email = request.getParameter("email");
        String redirectUrl = "/auth/login?error=true";
        
        try {
            // Verificar se é um erro específico de conta deletada
            if (exception instanceof UsernameNotFoundException && 
                exception.getMessage() != null && 
                exception.getMessage().contains("Conta deletada")) {
                redirectUrl = "/auth/login?error=deleted";
            } else if (email != null && !email.trim().isEmpty()) {
                // Verificar se o email existe mas a conta está deletada
                User user = userRepository.findByEmail(email.trim()).orElse(null);
                if (user != null && user.isDeleted()) {
                    redirectUrl = "/auth/login?error=deleted";
                }
            }
        } catch (Exception e) {
            // Em caso de erro, usar o comportamento padrão
            redirectUrl = "/auth/login?error=true";
        }
        
        response.sendRedirect(redirectUrl);
    }
} 