package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.ConfigHistory;
import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import com.example.PromptShieldAPI.repository.ConfigHistoryRepository;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
                .map(this::isModelActuallyEnabled)
                .orElse(false);
    }

    /**
     * Verifica se um modelo está realmente habilitado, considerando desligamentos temporários
     */
    private boolean isModelActuallyEnabled(SystemConfig config) {
        // Se não está habilitado, retorna false
        if (!config.isEnabled()) {
            return false;
        }

        // Se não está em desligamento temporário, retorna true
        if (!config.isTemporaryDisabled()) {
            return true;
        }

        // Se está em desligamento temporário, verifica se já expirou
        LocalDateTime now = LocalDateTime.now();
        if (config.getTemporaryDisabledEnd() != null && now.isAfter(config.getTemporaryDisabledEnd())) {
            // Desligamento temporário expirou, reativar automaticamente
            config.setTemporaryDisabled(false);
            config.setTemporaryDisabledStart(null);
            config.setTemporaryDisabledEnd(null);
            config.setTemporaryDisabledReason(null);
            repository.save(config);

            // Registrar no histórico
            ConfigHistory history = new ConfigHistory();
            history.setSystemConfig(config);
            history.setModel(config.getModel());
            history.setEnabled(true);
            history.setChangedBy("sistema (expiração automática)");
            configHistoryRepository.save(history);

            return true;
        }

        // Ainda está em desligamento temporário
        return false;
    }

    public void checkAndUpdateModelStatus(ModelType model) {
        // Se o modelo está em desligamento temporário, não verificar automaticamente
        SystemConfig config = repository.findByModel(model).orElse(null);
        if (config != null && config.isTemporaryDisabled()) {
            LocalDateTime now = LocalDateTime.now();
            if (config.getTemporaryDisabledEnd() != null && now.isBefore(config.getTemporaryDisabledEnd())) {
                // Ainda está em desligamento temporário, não verificar
                return;
            }
        }

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
                history.setSystemConfig(cfg);
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
            // Se está a ativar manualmente, remover desligamento temporário
            if (enabled) {
                cfg.setTemporaryDisabled(false);
                cfg.setTemporaryDisabledStart(null);
                cfg.setTemporaryDisabledEnd(null);
                cfg.setTemporaryDisabledReason(null);
            }
            repository.save(cfg);

            ConfigHistory history = new ConfigHistory();
            history.setSystemConfig(cfg);
            history.setModel(model);
            history.setEnabled(enabled);
            history.setChangedBy(changedBy);
            configHistoryRepository.save(history);
        });
    }

    /**
     * Desliga temporariamente um modelo LLM
     */
    public void temporarilyDisableModel(ModelType model, LocalDateTime disableUntil, String reason, String changedBy) {
        repository.findByModel(model).ifPresent(cfg -> {
            cfg.setEnabled(false);
            cfg.setTemporaryDisabled(true);
            cfg.setTemporaryDisabledStart(LocalDateTime.now());
            cfg.setTemporaryDisabledEnd(disableUntil);
            cfg.setTemporaryDisabledReason(reason);
            repository.save(cfg);

            // Registrar no histórico
            ConfigHistory history = new ConfigHistory();
            history.setSystemConfig(cfg);
            history.setModel(model);
            history.setEnabled(false);
            history.setChangedBy(changedBy + " (desligamento temporário até " + disableUntil + ")");
            configHistoryRepository.save(history);
        });
    }

    /**
     * Remove o desligamento temporário de um modelo
     */
    public void removeTemporaryDisable(ModelType model, String changedBy) {
        repository.findByModel(model).ifPresent(cfg -> {
            cfg.setTemporaryDisabled(false);
            cfg.setTemporaryDisabledStart(null);
            cfg.setTemporaryDisabledEnd(null);
            cfg.setTemporaryDisabledReason(null);
            // Não alterar o status enabled, apenas remover o desligamento temporário
            repository.save(cfg);

            // Registrar no histórico
            ConfigHistory history = new ConfigHistory();
            history.setSystemConfig(cfg);
            history.setModel(model);
            history.setEnabled(cfg.isEnabled());
            history.setChangedBy(changedBy + " (removido desligamento temporário)");
            configHistoryRepository.save(history);
        });
    }

    /**
     * Obtém informações sobre o desligamento temporário de um modelo
     */
    public SystemConfig getModelConfig(ModelType model) {
        return repository.findByModel(model).orElse(null);
    }
}
