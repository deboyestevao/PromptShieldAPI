package com.example.PromptShieldAPI.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor
@Data
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ModelType model;

    private boolean enabled;

    // Campos para desligamento temporário
    private boolean temporaryDisabled;
    private LocalDateTime temporaryDisabledStart;
    private LocalDateTime temporaryDisabledEnd;
    private String temporaryDisabledReason;
    
    // Campo para guardar o estado original antes do desligamento temporário
    private Boolean originalEnabledState;

    @Version
    private Long version;

    public enum ModelType {
        OLLAMA, OPENAI
    }
}
