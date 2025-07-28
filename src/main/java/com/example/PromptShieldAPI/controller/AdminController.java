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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SystemConfigService systemConfigService;
    private final UserRepository userRepository;

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
}
