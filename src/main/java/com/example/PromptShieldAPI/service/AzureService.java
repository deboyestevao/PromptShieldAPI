package com.example.PromptShieldAPI.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Service
@RequiredArgsConstructor
@RestController
public class AzureService {

    private final AzureOpenAiChatModel chatModel;
    private final OllamaChatModel ollamaChatModel;

    public String askOpenAi(String question) {
        return chatModel.call(question);
    }

    public String askOllama(String question) {
        return ollamaChatModel.call(question);
    }

    public boolean isOllamaReachable() {
        try {
            ollamaChatModel.call("ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOpenAiReachable() {
        try {
            chatModel.call("ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
