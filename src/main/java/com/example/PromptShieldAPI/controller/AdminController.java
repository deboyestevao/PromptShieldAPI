package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.SystemPreferencesRequest;
import com.example.PromptShieldAPI.dto.UserPreferencesRequest;
import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.UserPreferences;
import com.example.PromptShieldAPI.service.AdminService;
import com.example.PromptShieldAPI.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.repository.ChatRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SystemConfigService systemConfigService;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    @PatchMapping("/system-preferences")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSystemPreferences(@RequestBody SystemPreferencesRequest prefs) {
        try {
            SystemConfig updatedConfig = adminService.updateSystemPreferences(prefs.isOpenai(), prefs.isOllama());
            return ResponseEntity.ok(updatedConfig);
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(409).body("Error: System preferences were updated by another user. Please, try again.");
        }

    }

    @PostMapping("/user-preferences")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserPreferences(@RequestBody UserPreferencesRequest prefs) {
        UserPreferences updatedPreferences = adminService.updateUserPreferences(
                prefs.isOllamaPreferred(),
                prefs.isOpenaiPreferred(),
                prefs.getUserId()
        );
        return ResponseEntity.ok(updatedPreferences);
    }

    @PostMapping("/llm-user-prefs")
    public ResponseEntity<?> updateUserLLMPreferences(@RequestBody java.util.Map<String, Boolean> prefs) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        adminService.updateUserLLMPreferences(username, prefs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/llm-user-prefs")
    public ResponseEntity<?> getUserLLMPreferences() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(adminService.getUserLLMPreferences(username));
    }

    @GetMapping("/llm-status")
    public ResponseEntity<?> getLLMStatus() {
        boolean openai = systemConfigService.isModelEnabled(com.example.PromptShieldAPI.model.SystemConfig.ModelType.OPENAI);
        boolean ollama = systemConfigService.isModelEnabled(com.example.PromptShieldAPI.model.SystemConfig.ModelType.OLLAMA);
        return ResponseEntity.ok(java.util.Map.of("openai", openai, "ollama", ollama));
    }

    @GetMapping("/system-preferences")
    @PreAuthorize("hasRole('ADMIN')")
    public String systemPreferencesPage() {
        return "adminSystemPreferences";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "adminDashboard";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String usersPage() {
        return "adminUsers";
    }

    // API endpoints para gestão de utilizadores
    @GetMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> usersWithStats = users.stream().map(user -> {
                long chatCount = chatRepository.countByUser(user);
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
                userMap.put("lastName", user.getLastName() != null ? user.getLastName() : "");
                userMap.put("email", user.getEmail());
                userMap.put("username", user.getUsername());
                userMap.put("active", user.isActive());
                userMap.put("createdAt", user.getCreatedAt());
                userMap.put("chatCount", chatCount);
                return userMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(usersWithStats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            long chatCount = chatRepository.countByUser(user);
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            userData.put("email", user.getEmail());
            userData.put("username", user.getUsername());
            userData.put("active", user.isActive());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("chatCount", chatCount);
            userData.put("lastActivity", user.getLastLoginAt());
            
            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/api/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            user.setActive(true);
            userRepository.save(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilizador ativado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao ativar utilizador");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/api/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            user.setActive(false);
            userRepository.save(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilizador desativado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao desativar utilizador");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar se não é o próprio admin
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (user.getUsername().equals(currentUsername)) {
                Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Não pode eliminar a sua própria conta");
            return ResponseEntity.badRequest().body(errorResponse);
            }
            
            userRepository.delete(user);
            
            return ResponseEntity.ok(Map.of("message", "Utilizador eliminado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao eliminar utilizador"));
        }
    }
}
