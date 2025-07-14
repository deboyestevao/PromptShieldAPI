package com.example.PromptShieldAPI.dto;

import lombok.Data;

@Data
public class SystemPreferencesRequest {
    private boolean openai;
    private boolean ollama;
}
