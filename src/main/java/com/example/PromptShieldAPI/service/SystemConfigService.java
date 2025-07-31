package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.ConfigHistory;
import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import com.example.PromptShieldAPI.repository.ConfigHistoryRepository;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import com.example.PromptShieldAPI.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repository;
    private final ConfigHistoryRepository configHistoryRepository;
    private final AzureService azureService;
    private final ActivityLogService activityLogService;

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
     * Nota: A reativação automática agora é tratada pelo LLMAutoReactivationService em background
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
            // Desligamento temporário expirou, mas a reativação será tratada pelo serviço de background
            // Por enquanto, retorna false para manter a consistência
            return false;
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
            // Se está a ativar manualmente, remover desligamento temporário e limpar estado original
            if (enabled) {
                cfg.setTemporaryDisabled(false);
                cfg.setTemporaryDisabledStart(null);
                cfg.setTemporaryDisabledEnd(null);
                cfg.setTemporaryDisabledReason(null);
                cfg.setOriginalEnabledState(null); // Limpar estado original em alteração manual
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
            // Guardar o estado original antes do desligamento temporário
            cfg.setOriginalEnabledState(cfg.isEnabled());
            
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
            // Restaurar o estado original se existir
            Boolean originalState = cfg.getOriginalEnabledState();
            if (originalState != null) {
                cfg.setEnabled(originalState);
                cfg.setOriginalEnabledState(null); // Limpar o estado original
            }
            
            cfg.setTemporaryDisabled(false);
            cfg.setTemporaryDisabledStart(null);
            cfg.setTemporaryDisabledEnd(null);
            cfg.setTemporaryDisabledReason(null);
            repository.save(cfg);

            // Registrar no histórico
            ConfigHistory history = new ConfigHistory();
            history.setSystemConfig(cfg);
            history.setModel(model);
            history.setEnabled(cfg.isEnabled());
            String actionDescription = originalState != null ? 
                " (removido desligamento temporário - estado original restaurado: " + (originalState ? "ativo" : "inativo") + ")" :
                " (removido desligamento temporário)";
            history.setChangedBy(changedBy + actionDescription);
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
