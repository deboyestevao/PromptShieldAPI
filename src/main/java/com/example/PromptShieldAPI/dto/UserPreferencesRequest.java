package com.example.PromptShieldAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserPreferencesRequest {

    @Schema(description = "Sets user's AI models preferences", defaultValue = "true")
    private boolean openaiPreferred;
    @Schema(description = "Sets user's AI models preferences", defaultValue = "false")
    private boolean ollamaPreferred;

    @Schema(description = "To find user if needed", defaultValue = "1")
    private Long userId;
}