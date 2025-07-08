package com.example.SafeMindAPI.service;

import com.example.SafeMindAPI.model.SystemConfig;
import com.example.SafeMindAPI.model.User;
import com.example.SafeMindAPI.model.UserPreferences;
import com.example.SafeMindAPI.repository.SystemConfigRepository;
import com.example.SafeMindAPI.repository.UserPreferencesRepository;
import com.example.SafeMindAPI.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SystemConfigRepository systemConfigRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;

    @Transactional
    public SystemConfig updateSystemPreferences(boolean openaiEnabled, boolean ollamaEnabled) {
        Optional<SystemConfig> openaiConfigOpt = systemConfigRepository.findByModel(SystemConfig.ModelType.OPENAI);
        SystemConfig openaiConfig = openaiConfigOpt.orElse(new SystemConfig());
        openaiConfig.setModel(SystemConfig.ModelType.OPENAI);
        openaiConfig.setEnabled(openaiEnabled);
        systemConfigRepository.save(openaiConfig);

        Optional<SystemConfig> ollamaConfigOpt = systemConfigRepository.findByModel(SystemConfig.ModelType.OLLAMA);
        SystemConfig ollamaConfig = ollamaConfigOpt.orElse(new SystemConfig());
        ollamaConfig.setModel(SystemConfig.ModelType.OLLAMA);
        ollamaConfig.setEnabled(ollamaEnabled);
        systemConfigRepository.save(ollamaConfig);

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
}
