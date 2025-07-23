package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.QuestionWithFilesRequest;
import com.example.PromptShieldAPI.dto.QuestionYearMonthRequest;
import com.example.PromptShieldAPI.service.AiService;
import com.example.PromptShieldAPI.service.FileService;
import com.example.PromptShieldAPI.service.SystemConfigService;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiControllerTest {

    @Mock
    private AiService aiService;

    @Mock
    private SystemConfigService configService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private AiController aiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testWelcome() {
        String result = aiController.welcome();
        assertEquals("Bem vindo/a testUser", result);
    }

    @Test
    void testAsk_withOpenAIEnabled() {
        QuestionWithFilesRequest request = new QuestionWithFilesRequest();
        request.setQuestion("Qual é a capital da França?");
        request.setFileIds(List.of());

        when(configService.isModelEnabled(ModelType.OPENAI)).thenReturn(true);
        when(configService.isModelEnabled(ModelType.OLLAMA)).thenReturn(false);
        when(fileService.estimateTokens(anyString())).thenReturn(1000);
        when(aiService.askOpenAi(anyString())).thenReturn("Paris");

        String response = aiController.ask(request);
        assertTrue(response.contains("OpenAI: Paris"));
    }

    @Test
    void testAsk_noModelsEnabled() {
        QuestionWithFilesRequest request = new QuestionWithFilesRequest();
        request.setQuestion("Pergunta?");
        request.setFileIds(null);

        when(configService.isModelEnabled(ModelType.OPENAI)).thenReturn(false);
        when(configService.isModelEnabled(ModelType.OLLAMA)).thenReturn(false);

        String response = aiController.ask(request);
        assertEquals("Nenhum LLM está ativado na configuração do sistema.", response);
    }

    @Test
    void testFindByDate() {
        QuestionYearMonthRequest mockResult = new QuestionYearMonthRequest();
        when(aiService.findByYearAndMonth(2023, 6)).thenReturn(ResponseEntity.ok(mockResult));

        ResponseEntity<QuestionYearMonthRequest> response = aiController.findByDate(2023, 6);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResult, response.getBody());
    }
}
