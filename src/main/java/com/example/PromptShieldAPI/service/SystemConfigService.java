package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repository;
    private final AzureService azureService;

    public boolean isOllamaEnabled() {
        checkAndUpdateModelStatus(ModelType.OLLAMA);
        return isModelEnabled(ModelType.OLLAMA);
    }

    public boolean isOpenAiEnabled() {
        checkAndUpdateModelStatus(ModelType.OPENAI);
        return isModelEnabled(ModelType.OPENAI);
    }

    public boolean isModelEnabled(ModelType model) {
        return repository.findByModel(model)
                .map(SystemConfig::isEnabled)
                .orElse(false);
    }

    public void checkAndUpdateModelStatus(ModelType model) {
        boolean reachable = switch (model) {
            case OLLAMA -> azureService.isOllamaReachable();
            case OPENAI -> azureService.isOpenAiReachable();
        };

        repository.findByModel(model).ifPresent(cfg -> {
            if (cfg.isEnabled() != reachable) {
                cfg.setEnabled(reachable);
                repository.save(cfg);
            }
        });
    }
}
