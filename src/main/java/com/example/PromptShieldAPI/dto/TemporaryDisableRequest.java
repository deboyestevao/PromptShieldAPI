package com.example.PromptShieldAPI.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TemporaryDisableRequest {
    private String model; // "OPENAI" ou "OLLAMA"
    private LocalDateTime disableUntil;
    private String reason;
} 