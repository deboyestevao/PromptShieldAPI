package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserPreferencesRepository repository;

    public boolean isUserPreferenceEnabled(User user, String model) {
        return repository.findByUser(user)
                .map(prefs -> {
                    return switch (model.toLowerCase()) {
                        case "openai" -> prefs.isOpenaiPreferred();
                        case "ollama" -> prefs.isOllamaPreferred();
                        default -> false;
                    };
                })
                .orElse(false);
    }
}
