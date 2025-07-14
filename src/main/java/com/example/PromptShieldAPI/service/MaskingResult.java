package com.example.PromptShieldAPI.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class MaskingResult {

    private String maskedText;
    private Long total;

    public MaskingResult(String maskedText, Long total) {
        this.maskedText = maskedText;
        this.total = total;
    }
}
