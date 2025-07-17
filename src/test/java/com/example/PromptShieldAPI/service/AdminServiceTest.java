package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.model.UserPreferences;
import com.example.PromptShieldAPI.repository.SystemConfigRepository;
import com.example.PromptShieldAPI.repository.UserPreferencesRepository;
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
class AdminServiceTest {

    @Mock private SystemConfigRepository systemConfigRepository;
    @Mock private UserPreferencesRepository userPreferencesRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AdminService adminService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void testUpdateSystemPreferences_WhenConfigsExist() {
        SystemConfig openai = new SystemConfig();
        openai.setModel(SystemConfig.ModelType.OPENAI);
        openai.setEnabled(false);

        SystemConfig ollama = new SystemConfig();
        ollama.setModel(SystemConfig.ModelType.OLLAMA);
        ollama.setEnabled(false);

        when(systemConfigRepository.findByModel(SystemConfig.ModelType.OPENAI)).thenReturn(Optional.of(openai));
        when(systemConfigRepository.findByModel(SystemConfig.ModelType.OLLAMA)).thenReturn(Optional.of(ollama));

        SystemConfig result = adminService.updateSystemPreferences(true, true);

        assertTrue(result.isEnabled());
        verify(systemConfigRepository, times(2)).save(any(SystemConfig.class));
    }

    @Test
    void testUpdateSystemPreferences_WhenConfigsDoNotExist() {
        when(systemConfigRepository.findByModel(SystemConfig.ModelType.OPENAI)).thenReturn(Optional.empty());
        when(systemConfigRepository.findByModel(SystemConfig.ModelType.OLLAMA)).thenReturn(Optional.empty());

        SystemConfig result = adminService.updateSystemPreferences(true, false);

        assertEquals(SystemConfig.ModelType.OPENAI, result.getModel());
        assertTrue(result.isEnabled());
        verify(systemConfigRepository, times(2)).save(any(SystemConfig.class));
    }

    @Test
    void testUpdateUserPreferences_WhenPreferencesExist() {
        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        prefs.setOpenaiPreferred(false);
        prefs.setOllamaPreferred(false);

        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(userPreferencesRepository.findByUser(user)).thenReturn(Optional.of(prefs));

        UserPreferences result = adminService.updateUserPreferences(true, true, 1L);

        assertTrue(result.isOpenaiPreferred());
        assertTrue(result.isOllamaPreferred());
        verify(userPreferencesRepository).save(result);
    }

    @Test
    void testUpdateUserPreferences_WhenPreferencesDoNotExist() {
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(userPreferencesRepository.findByUser(user)).thenReturn(Optional.empty());

        UserPreferences result = adminService.updateUserPreferences(false, true, 1L);

        assertFalse(result.isOllamaPreferred());
        assertTrue(result.isOpenaiPreferred());
        assertEquals(user, result.getUser());
        verify(userPreferencesRepository).save(result);
    }
}
