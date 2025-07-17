package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.dto.QuestionYearMonthRequest;
import com.example.PromptShieldAPI.interfaces.QuestionService;
import com.example.PromptShieldAPI.model.Question;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.model.UserPreferences;
import com.example.PromptShieldAPI.repository.QuestionRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock private AzureOpenAiChatModel openAiModel;
    @Mock private OllamaChatModel ollamaModel;
    @Mock private QuestionRepository questionRepo;
    @Mock private UserRepository userRepo;
    @Mock private SystemConfigService configService;
    @Mock private QuestionService questionService;

    @InjectMocks private AiService aiService;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setUsername("testUser");

        UserPreferences prefs = new UserPreferences();
        prefs.setOpenaiPreferred(true);
        prefs.setOllamaPreferred(true);
        mockUser.setPreferences(prefs);
    }

    private void mockSecurityContext() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testUser");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
    }

    @Test
    void testAskOpenAi_WhenEnabledAndPreferred() {
        mockSecurityContext();
        when(configService.isOpenAiEnabled()).thenReturn(true);
        when(openAiModel.call(anyString())).thenReturn("Resposta da OpenAI");

        String result = aiService.askOpenAi("Qual é a capital de Portugal?");
        assertTrue(result.contains("Resposta da OpenAI"));
    }

    @Test
    void testAskOpenAi_WhenDisabledGlobally() {
        when(configService.isOpenAiEnabled()).thenReturn(false);

        String result = aiService.askOpenAi("Teste");
        assertEquals("OpenAi está em manutenção.", result);
    }

    @Test
    void testAskOpenAi_WhenUserHasDisabledPreference() {
        mockUser.getPreferences().setOpenaiPreferred(false);
        mockSecurityContext();
        when(configService.isOpenAiEnabled()).thenReturn(true);

        String result = aiService.askOpenAi("Teste");
        assertEquals("OpenAi está desativado para este utilizador.", result);
    }

    @Test
    void testAskOllama_WhenEnabledAndPreferred() {
        mockSecurityContext();
        when(configService.isOllamaEnabled()).thenReturn(true);
        when(ollamaModel.call(anyString())).thenReturn("Resposta da Ollama");

        String result = aiService.askOllama("Qual é a capital da França?");
        assertTrue(result.contains("Resposta da Ollama"));
    }

    @Test
    void testFindByYearAndMonth_ReturnsQuestions() {
        Question q = new Question();
        q.setQuestion("Pergunta?");
        q.setAnswer("Resposta.");
        q.setModel("openai");
        q.setDate(LocalDateTime.now());
        q.setUser(mockUser);

        when(questionRepo.findByDateBetween(any(), any())).thenReturn(List.of(q));

        ResponseEntity<QuestionYearMonthRequest> response = aiService.findByYearAndMonth(2025, 7);
        assertEquals(2025, response.getBody().getYear());
        assertEquals(7, response.getBody().getMonth());
        assertEquals(1, response.getBody().getQuestions().size());
    }

    @Test
    void testAskOpenAi_WhenUserPreferencesIsNull() {
        mockUser.setPreferences(null);
        mockSecurityContext();
        when(configService.isOpenAiEnabled()).thenReturn(true);

        String result = aiService.askOpenAi("Teste");
        assertEquals("OpenAi está desativado para este utilizador.", result);
    }

    @Test
    void testAskOllama_WhenUserPreferencesIsNull() {
        mockUser.setPreferences(null);
        mockSecurityContext();
        when(configService.isOllamaEnabled()).thenReturn(true);

        String result = aiService.askOllama("Teste");
        assertEquals("Ollama está desativado para este utilizador.", result);
    }

    @Test
    void testAskOpenAi_WhenSensitiveDataIsDetected() {
        mockSecurityContext();
        when(configService.isOpenAiEnabled()).thenReturn(true);
        when(openAiModel.call(anyString())).thenReturn("Resposta da OpenAI");

        // Simula que a pergunta contém 2 dados sensíveis
        String pergunta = "O meu NIF é 123456789 e o meu IBAN é PT50000201231234567890154";
        String result = aiService.askOpenAi(pergunta);

        assertTrue(result.contains("Resposta da OpenAI"));
        assertTrue(result.contains("Foram encontrados 2 dados sensíveis"));
    }

    @Test
    void testAskOllama_WhenSensitiveDataIsDetected() {
        mockSecurityContext();
        when(configService.isOllamaEnabled()).thenReturn(true);
        when(ollamaModel.call(anyString())).thenReturn("Resposta da Ollama");

        String pergunta = "O meu número de cartão é 1234 5678 9012 3456";
        String result = aiService.askOllama(pergunta);

        assertTrue(result.contains("Resposta da Ollama"));
        assertTrue(result.contains("Foi encontrado 1 dado sensível"));
    }

    @Test
    void testFindByYearAndMonth_WhenNoQuestionsFound() {
        when(questionRepo.findByDateBetween(any(), any())).thenReturn(List.of());

        ResponseEntity<QuestionYearMonthRequest> response = aiService.findByYearAndMonth(2025, 7);
        assertEquals(2025, response.getBody().getYear());
        assertEquals(7, response.getBody().getMonth());
        assertTrue(response.getBody().getQuestions().isEmpty());
    }

    @Test
    void testAskOpenAi_WhenNoSensitiveDataFound() {
        mockSecurityContext();
        when(configService.isOpenAiEnabled()).thenReturn(true);
        when(openAiModel.call(anyString())).thenReturn("Resposta limpa");

        String pergunta = "Qual é a capital de Espanha?";
        String result = aiService.askOpenAi(pergunta);

        assertTrue(result.contains("Resposta limpa"));
        assertFalse(result.contains("dados sensíveis"));
    }

    @Test
    void testAskOllama_WhenNoSensitiveDataFound() {
        mockSecurityContext();
        when(configService.isOllamaEnabled()).thenReturn(true);
        when(ollamaModel.call(anyString())).thenReturn("Resposta limpa");

        String pergunta = "Qual é a capital da Alemanha?";
        String result = aiService.askOllama(pergunta);

        assertTrue(result.contains("Resposta limpa"));
        assertFalse(result.contains("dados sensíveis"));
    }

    @Test
    void testGetCurrentUser_WhenUserNotFound_ShouldThrow() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("inexistente");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(configService.isOpenAiEnabled()).thenReturn(true);
        when(userRepo.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            aiService.askOpenAi("Teste");
        });
    }


    @Test
    void testFindByYearAndMonth_WithInvalidMonth() {
        assertThrows(DateTimeException.class, () -> {
            aiService.findByYearAndMonth(2025, 13);
        });
    }

    @Test
    void testAskOpenAi_ShouldCallSaveQuestion() {
        mockSecurityContext();
        when(configService.isOpenAiEnabled()).thenReturn(true);
        when(openAiModel.call(anyString())).thenReturn("Resposta");

        aiService.askOpenAi("Qual é o teu nome?");

        verify(questionService).saveQuestion(anyString(), anyString(), eq("openai"));
    }

}
