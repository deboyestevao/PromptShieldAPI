package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.SystemPreferencesRequest;
import com.example.PromptShieldAPI.dto.UserPreferencesRequest;
import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.UserPreferences;
import com.example.PromptShieldAPI.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void updateSystemPreferences_shouldReturnUpdatedConfig() {
        SystemPreferencesRequest request = new SystemPreferencesRequest();
        request.setOpenai(true);
        request.setOllama(false);

        SystemConfig config = new SystemConfig(); // mock config

        when(adminService.updateSystemPreferences(true, false)).thenReturn(config);

        ResponseEntity<?> response = adminController.updateSystemPreferences(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(config, response.getBody());
    }

    @Test
    void updateSystemPreferences_shouldReturnConflictOnOptimisticLockingFailure() {
        SystemPreferencesRequest request = new SystemPreferencesRequest();
        request.setOpenai(true);
        request.setOllama(false);

        when(adminService.updateSystemPreferences(true, false))
                .thenThrow(new OptimisticLockingFailureException("Conflict"));

        ResponseEntity<?> response = adminController.updateSystemPreferences(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("System preferences were updated by another user"));
    }

}
