package com.example.SafeMindAPI.service;

import com.example.SafeMindAPI.model.SystemConfig;
import com.example.SafeMindAPI.model.SystemConfig.ModelType;
import com.example.SafeMindAPI.repository.SystemConfigRepository;
import com.example.SafeMindAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repository;
    private final UserRepository userRepository;

    public boolean isOpenAiEnabled() {
        return isModelEnabled(ModelType.OPENAI);
    }

    public boolean isOllamaEnabled() {
        return isModelEnabled(ModelType.OLLAMA);
    }

    private boolean isModelEnabled(ModelType model) {
        return repository.findByModel(model)
                .map(SystemConfig::isEnabled)
                .orElse(false);
    }
}
