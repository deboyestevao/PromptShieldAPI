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
    private Map<String, Integer> counts;

    public MaskingResult(String maskedText, Map<String, Integer> counts) {
        this.maskedText = maskedText;
        this.counts = counts;
    }

}
