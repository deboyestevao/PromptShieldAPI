package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.ConfigHistory;
import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import com.example.PromptShieldAPI.repository.ConfigHistoryRepository;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repository;
    private final ConfigHistoryRepository configHistoryRepository;
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
            boolean previousStatus = cfg.isEnabled();

            if (previousStatus != reachable) {
                cfg.setEnabled(reachable);
                repository.save(cfg);

                // ✅ Registrar no histórico
                ConfigHistory history = new ConfigHistory();
                history.setModel(model);
                history.setEnabled(reachable);
                history.setChangedBy("sistema");
                configHistoryRepository.save(history);
            }
        });
    }

    // (opcional) usar em alterações manuais também
    public void updateModelStatusManually(ModelType model, boolean enabled, String changedBy) {
        repository.findByModel(model).ifPresent(cfg -> {
            cfg.setEnabled(enabled);
            repository.save(cfg);

            ConfigHistory history = new ConfigHistory();
            history.setModel(model);
            history.setEnabled(enabled);
            history.setChangedBy(changedBy);
            configHistoryRepository.save(history);
        });
    }
}
