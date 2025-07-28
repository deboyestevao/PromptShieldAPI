package com.example.PromptShieldAPI.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class ConfigHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Relacionamento com SystemConfig
    @ManyToOne
    @JoinColumn(name = "system_config_id")
    private SystemConfig systemConfig;

    @Enumerated(EnumType.STRING)
    private SystemConfig.ModelType model;

    private boolean enabled;

    private String changedBy; // "sistema" ou nome do admin

    @CreationTimestamp
    private LocalDateTime changedAt;
}
