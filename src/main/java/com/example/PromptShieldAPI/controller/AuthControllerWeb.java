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

    @PostMapping("/login")
    public String processLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        // Atualizar last_login_at quando o login for bem-sucedido
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
        
        // O Spring Security vai processar a autenticação automaticamente
        return "redirect:/chat";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
