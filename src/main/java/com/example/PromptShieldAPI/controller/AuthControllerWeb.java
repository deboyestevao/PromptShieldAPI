package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/auth")
public class AuthControllerWeb {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }





    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Marcar utilizador como offline antes de invalidar a sessão
        try {
            String username = session.getAttribute("username") != null ? 
                session.getAttribute("username").toString() : null;
            
            if (username != null) {
                userRepository.findByUsername(username).ifPresent(user -> {
                    user.setLastActive(LocalDateTime.now());
                    userRepository.save(user);
                });
            }
        } catch (Exception e) {
            // Log error but don't fail logout
            System.err.println("Erro ao marcar utilizador como offline: " + e.getMessage());
        }
        
        session.invalidate();
        return "redirect:/auth/login";
    }
}
