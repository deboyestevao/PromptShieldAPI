package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceTest {

    @Mock private SystemConfigRepository repository;
    @Mock private UserRepository userRepository;

    @InjectMocks private SystemConfigService service;

    @Test
    void testIsOpenAiEnabled_WhenEnabled() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OPENAI);
        config.setEnabled(true);

        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.of(config));

        assertTrue(service.isOpenAiEnabled());
    }

    @Test
    void testIsOpenAiEnabled_WhenDisabled() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OPENAI);
        config.setEnabled(false);

        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.of(config));

        assertFalse(service.isOpenAiEnabled());
    }

    @Test
    void testIsOpenAiEnabled_WhenNotFound() {
        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.empty());

        assertFalse(service.isOpenAiEnabled());
    }

    @Test
    void testIsOllamaEnabled_WhenEnabled() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OLLAMA);
        config.setEnabled(true);

        when(repository.findByModel(ModelType.OLLAMA)).thenReturn(Optional.of(config));

        assertTrue(service.isOllamaEnabled());
    }

    @Test
    void testIsOllamaEnabled_WhenDisabled() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OLLAMA);
        config.setEnabled(false);

        when(repository.findByModel(ModelType.OLLAMA)).thenReturn(Optional.of(config));

        assertFalse(service.isOllamaEnabled());
    }

    @Test
    void testIsOllamaEnabled_WhenNotFound() {
        when(repository.findByModel(ModelType.OLLAMA)).thenReturn(Optional.empty());

        assertFalse(service.isOllamaEnabled());
    }

    @Test
    void testIsOpenAiEnabled_WhenEnabledIsNull_ShouldReturnFalse() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OPENAI);
        config.setEnabled(false); // simula valor nulo

        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.of(config));

        assertFalse(service.isOpenAiEnabled());
    }

    @Test
    void testMultipleModelChecks_ShouldBeIndependent() {
        SystemConfig openai = new SystemConfig();
        openai.setModel(ModelType.OPENAI);
        openai.setEnabled(true);

        SystemConfig ollama = new SystemConfig();
        ollama.setModel(ModelType.OLLAMA);
        ollama.setEnabled(false);

        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.of(openai));
        when(repository.findByModel(ModelType.OLLAMA)).thenReturn(Optional.of(ollama));

        assertTrue(service.isOpenAiEnabled());
        assertFalse(service.isOllamaEnabled());
    }


}
