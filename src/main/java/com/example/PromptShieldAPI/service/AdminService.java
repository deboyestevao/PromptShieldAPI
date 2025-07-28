package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.model.UserPreferences;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import com.example.PromptShieldAPI.repository.UserPreferencesRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SystemConfigRepository systemConfigRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;
    private final SystemConfigService systemConfigService;

    @Transactional
    public SystemConfig updateSystemPreferences(boolean openaiEnabled, boolean ollamaEnabled) {
        String adminName = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<SystemConfig> openaiConfigOpt = systemConfigRepository.findByModel(SystemConfig.ModelType.OPENAI);
        SystemConfig openaiConfig = openaiConfigOpt.orElse(new SystemConfig());
        openaiConfig.setModel(SystemConfig.ModelType.OPENAI);
        openaiConfig.setEnabled(openaiEnabled);
        systemConfigRepository.save(openaiConfig);
        // Registrar histórico
        systemConfigService.updateModelStatusManually(SystemConfig.ModelType.OPENAI, openaiEnabled, adminName);

        Optional<SystemConfig> ollamaConfigOpt = systemConfigRepository.findByModel(SystemConfig.ModelType.OLLAMA);
        SystemConfig ollamaConfig = ollamaConfigOpt.orElse(new SystemConfig());
        ollamaConfig.setModel(SystemConfig.ModelType.OLLAMA);
        ollamaConfig.setEnabled(ollamaEnabled);
        systemConfigRepository.save(ollamaConfig);
        // Registrar histórico
        systemConfigService.updateModelStatusManually(SystemConfig.ModelType.OLLAMA, ollamaEnabled, adminName);

        return openaiConfig;
    }

    @Transactional
    public UserPreferences updateUserPreferences(boolean ollamaPreferred, boolean openaiPreferred, Long id) {
        User user = userRepository.getReferenceById(id);
        Optional<UserPreferences> userPreferencesOpt = userPreferencesRepository.findByUser(user);
        UserPreferences userPreferences = userPreferencesOpt.orElse(new UserPreferences());

        userPreferences.setOpenaiPreferred(openaiPreferred);
        userPreferences.setOllamaPreferred(ollamaPreferred);
        userPreferences.setUser(user);

        userPreferencesRepository.save(userPreferences);
        return userPreferences;
    }

    @Transactional
    public void updateUserLLMPreferences(String username, java.util.Map<String, Boolean> prefs) {
        User user = userRepository.findByUsername(username).orElseThrow();
        boolean openai = prefs.getOrDefault("openai", false);
        boolean ollama = prefs.getOrDefault("ollama", false);
        updateUserPreferences(ollama, openai, user.getId());
    }

    public java.util.Map<String, Boolean> getUserLLMPreferences(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Optional<UserPreferences> prefsOpt = userPreferencesRepository.findByUser(user);
        boolean openai = prefsOpt.map(UserPreferences::isOpenaiPreferred).orElse(false);
        boolean ollama = prefsOpt.map(UserPreferences::isOllamaPreferred).orElse(false);
        return java.util.Map.of("openai", openai, "ollama", ollama);
    }
}
