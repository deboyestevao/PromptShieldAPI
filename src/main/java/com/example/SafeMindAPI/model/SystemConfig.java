package com.example.SafeMindAPI.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Version
    private Long version;

    public enum ModelType {
        OLLAMA, OPENAI
    }
}
