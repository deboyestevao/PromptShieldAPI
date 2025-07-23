package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user123");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testUploadFiles() {
        MultipartFile[] files = new MultipartFile[0];

        List<String> mockIds = List.of("file1", "file2");
        when(fileService.saveFiles(files, "user123")).thenReturn(mockIds);

        ResponseEntity<List<String>> response = fileController.uploadFiles(files);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockIds, response.getBody());
    }
}
