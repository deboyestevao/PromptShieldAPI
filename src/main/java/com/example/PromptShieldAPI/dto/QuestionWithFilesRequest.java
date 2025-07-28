package com.example.PromptShieldAPI.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionWithFilesRequest {
    private String question;
    private List<String> fileIds;
    private Long chatId;

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
}
