package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/chat")
    public String chat(Model model) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user != null) {
                model.addAttribute("username", user.getFirstName());
            } else {
                model.addAttribute("username", "utilizador");
            }
        } catch (Exception e) {
            model.addAttribute("username", "utilizador");
        }
        
        return "chat";
    }
}