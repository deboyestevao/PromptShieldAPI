package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.ConfigHistory;
import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import com.example.PromptShieldAPI.repository.ConfigHistoryRepository;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMAutoReactivationService {

    private final SystemConfigRepository systemConfigRepository;
    private final ConfigHistoryRepository configHistoryRepository;
    private final ActivityLogService activityLogService;

    /**
     * Verifica e reativa automaticamente LLMs que tiveram o desligamento temporário expirado
     * Executa a cada minuto
     */
    @Scheduled(fixedRate = 60000) // 60 segundos = 1 minuto
    public void checkAndReactivateExpiredLLMs() {
        try {
            log.debug("Verificando LLMs com desligamento temporário expirado...");
            
            LocalDateTime now = LocalDateTime.now();
            
            // Buscar todas as configurações com desligamento temporário
            List<SystemConfig> temporaryDisabledConfigs = systemConfigRepository
                .findByTemporaryDisabledTrue();
            
            for (SystemConfig config : temporaryDisabledConfigs) {
                if (config.getTemporaryDisabledEnd() != null && 
                    now.isAfter(config.getTemporaryDisabledEnd())) {
                    
                    log.info("Reativando automaticamente LLM {} - desligamento temporário expirado", 
                        config.getModel().name());
                    
                    reactivateLLM(config);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao verificar e reativar LLMs automaticamente", e);
        }
    }

    /**
     * Reativa uma LLM que teve o desligamento temporário expirado
     */
    private void reactivateLLM(SystemConfig config) {
        try {
            // Restaurar o estado original da LLM
            Boolean originalState = config.getOriginalEnabledState();
            boolean restoredState = originalState != null ? originalState : true; // Default para true se não houver estado original
            
            config.setEnabled(restoredState);
            config.setTemporaryDisabled(false);
            config.setTemporaryDisabledStart(null);
            config.setTemporaryDisabledEnd(null);
            config.setTemporaryDisabledReason(null);
            config.setOriginalEnabledState(null); // Limpar o estado original
            systemConfigRepository.save(config);

            // Registrar no histórico
            ConfigHistory history = new ConfigHistory();
            history.setSystemConfig(config);
            history.setModel(config.getModel());
            history.setEnabled(restoredState);
            history.setChangedBy("sistema (reativação automática - estado original restaurado: " + (restoredState ? "ativo" : "inativo") + ")");
            configHistoryRepository.save(history);

            // Registrar atividade no log
            String modelName = config.getModel() == ModelType.OPENAI ? "OpenAI" : "Ollama";
            String stateDescription = restoredState ? "ativado" : "desativado";
            activityLogService.logActivity(
                "LLM_AUTO_REACTIVATED",
                "LLM Reativada Automaticamente",
                "Modelo " + modelName + " foi automaticamente reativado para o estado original (" + stateDescription + ") após expiração do desligamento temporário",
                "sistema"
            );

            log.info("LLM {} reativada automaticamente para o estado original: {}", config.getModel().name(), restoredState);
            
        } catch (Exception e) {
            log.error("Erro ao reativar LLM {} automaticamente", config.getModel().name(), e);
        }
    }

    /**
     * Método para verificação manual (pode ser chamado via API se necessário)
     */
    public void manualCheckAndReactivate() {
        log.info("Executando verificação manual de LLMs para reativação automática");
        checkAndReactivateExpiredLLMs();
    }
} 