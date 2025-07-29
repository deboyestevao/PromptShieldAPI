package com.example.PromptShieldAPI.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class MaskingResult {

    private String maskedText;
    private Long total = 0L; // Sempre 0, não conta dados sensíveis

    public MaskingResult(String maskedText, Long total) {
        this.maskedText = maskedText;
        this.total = 0L; // Ignora o total passado, sempre 0
    }
    
    public MaskingResult(String maskedText) {
        this.maskedText = maskedText;
        this.total = 0L;
    }
}
