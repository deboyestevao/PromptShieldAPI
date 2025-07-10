package com.example.SafeMindAPI.service;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

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
