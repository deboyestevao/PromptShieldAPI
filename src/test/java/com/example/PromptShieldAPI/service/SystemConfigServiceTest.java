package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
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
    @Mock private AzureService azureService;

    @InjectMocks private SystemConfigService service;

    @Test
    void testIsOpenAiEnabled_WhenEnabled() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OPENAI);
        config.setEnabled(true);

        when(azureService.isOpenAiReachable()).thenReturn(true); // também true
        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.of(config));

        boolean result = service.isOpenAiEnabled();

        assertTrue(result);
        verify(repository, never()).save(any());
    }


    @Test
    void testIsOpenAiEnabled_WhenDisabled() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OPENAI);
        config.setEnabled(false);

        when(azureService.isOpenAiReachable()).thenReturn(false);
        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.of(config));

        assertFalse(service.isOpenAiEnabled());
        verify(repository, never()).save(any());
    }

    @Test
    void testIsOpenAiEnabled_WhenNotFound() {
        when(azureService.isOpenAiReachable()).thenReturn(true);
        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.empty());

        assertFalse(service.isOpenAiEnabled());
        verify(repository, never()).save(any());
    }

    @Test
    void testIsOllamaEnabled_WhenEnabled() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OLLAMA);
        config.setEnabled(true); // já está igual ao reachability

        when(azureService.isOllamaReachable()).thenReturn(true);
        when(repository.findByModel(ModelType.OLLAMA)).thenReturn(Optional.of(config));

        boolean result = service.isOllamaEnabled();

        assertTrue(result);
        verify(repository, never()).save(any());
    }


    @Test
    void testIsOllamaEnabled_WhenDisabled() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OLLAMA);
        config.setEnabled(false);

        when(azureService.isOllamaReachable()).thenReturn(false);
        when(repository.findByModel(ModelType.OLLAMA)).thenReturn(Optional.of(config));

        assertFalse(service.isOllamaEnabled());
        verify(repository, never()).save(any());
    }

    @Test
    void testIsOllamaEnabled_WhenNotFound() {
        when(azureService.isOllamaReachable()).thenReturn(true);
        when(repository.findByModel(ModelType.OLLAMA)).thenReturn(Optional.empty());

        assertFalse(service.isOllamaEnabled());
        verify(repository, never()).save(any());
    }

    @Test
    void testIsOpenAiEnabled_WhenEnabledChangesToMatchReachable() {
        SystemConfig config = new SystemConfig();
        config.setModel(ModelType.OPENAI);
        config.setEnabled(false); // diferente do retorno do azure

        when(azureService.isOpenAiReachable()).thenReturn(true);
        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.of(config));

        assertTrue(service.isOpenAiEnabled());
        verify(repository).save(config);
        assertTrue(config.isEnabled());
    }

    @Test
    void testCheckAndUpdateModelStatus_WhenModelNotFound_ShouldNotThrow() {
        when(azureService.isOpenAiReachable()).thenReturn(true);
        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.checkAndUpdateModelStatus(ModelType.OPENAI));
        verify(repository, never()).save(any());
    }

    @Test
    void testMultipleModelChecks_ShouldBeIndependent() {
        SystemConfig openai = new SystemConfig();
        openai.setModel(ModelType.OPENAI);
        openai.setEnabled(false); // <- diferente do retorno simulado (true)

        SystemConfig ollama = new SystemConfig();
        ollama.setModel(ModelType.OLLAMA);
        ollama.setEnabled(true); // <- diferente do retorno simulado (false)

        when(azureService.isOpenAiReachable()).thenReturn(true);
        when(azureService.isOllamaReachable()).thenReturn(false);

        when(repository.findByModel(ModelType.OPENAI)).thenReturn(Optional.of(openai));
        when(repository.findByModel(ModelType.OLLAMA)).thenReturn(Optional.of(ollama));

        assertTrue(service.isOpenAiEnabled());    // mudará de false -> true
        assertFalse(service.isOllamaEnabled());   // mudará de true -> false

        verify(repository).save(openai);
        verify(repository).save(ollama);
    }

}
