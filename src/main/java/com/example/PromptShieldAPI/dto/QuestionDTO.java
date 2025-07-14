package com.example.PromptShieldAPI.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionDTO {
    private String question;
    private String answer;
    private String model;
    private LocalDateTime date;
    private String username;

    public String getUsername() {
        return maskUsername(this.username);
    }

    private String maskUsername(String originalUsername) {
        if (originalUsername == null) return null;
        return "*".repeat(originalUsername.length());
    }

}
