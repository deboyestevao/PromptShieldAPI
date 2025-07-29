package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.dto.QuestionDTO;
import com.example.PromptShieldAPI.dto.QuestionYearMonthRequest;
import com.example.PromptShieldAPI.interfaces.QuestionService;
import com.example.PromptShieldAPI.model.Question;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.QuestionRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.util.DataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AzureOpenAiChatModel chatModel;
    private final OllamaChatModel ollamaChatModel;
    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final SystemConfigService systemConfigService;
    private final QuestionService questionService;

    private static final String INPUT_FOLDER = "InputFiles";

    public String askOpenAi(String question, Long chatId) {
        // log.info("Iniciando askOpenAi - chatId: {}, question: {}", chatId, question.substring(0, Math.min(50, question.length())));
        
        if (!systemConfigService.isOpenAiEnabled()) {
            return "OpenAi está em manutenção.";
        }

        User user = getCurrentUser();
        if (user.getPreferences() == null || !user.getPreferences().isOpenaiPreferred()) {
            return "OpenAi está desativado para este utilizador.";
        }

        // Carregar histórico do chat para contexto
        String context = buildChatContext(chatId);
        String finalPrompt = context + "Usuário: " + question + "\nOpenAI: ";
        
        // log.info("Prompt final para OpenAI: {}", finalPrompt.substring(0, Math.min(100, finalPrompt.length())));

        MaskingResult maskingResult = DataMasker.maskSensitiveData(question);
        String maskedQuestion = maskingResult.getMaskedText();
        long total = maskingResult.getTotal();

        try {
            String answer = chatModel.call(finalPrompt) + "\n";
            questionService.saveQuestion(maskedQuestion, answer, "openai", chatId);

            if (total > 0) {
                answer += (total > 1 ? "Foram encontrados " : "Foi encontrado ")
                        + total + " dado" + (total > 1 ? "s " : " ")
                        + (total > 1 ? "sensíveis" : "sensível")
                        + " na tua mensagem. Os dados foram mascarados por questões de segurança.\n";
            }

            // log.info("Resposta OpenAI gerada com sucesso");
            return answer;
        } catch (Exception e) {
            log.error("Erro ao chamar OpenAI: {}", e.getMessage(), e);
            return "Erro ao processar a pergunta: " + e.getMessage();
        }
    }

    public String askOllama(String question, Long chatId) {
        // log.info("Iniciando askOllama - chatId: {}, question: {}", chatId, question.substring(0, Math.min(50, question.length())));
        
        if (!systemConfigService.isOllamaEnabled()) {
            return "Ollama está em manutenção.";
        }

        User user = getCurrentUser();
        if (user.getPreferences() == null || !user.getPreferences().isOllamaPreferred()) {
            return "Ollama está desativado para este utilizador.";
        }

        // Carregar histórico do chat para contexto
        String context = buildChatContext(chatId);
        String finalPrompt = context + "Usuário: " + question + "\nOllama: ";
        
        // log.info("Prompt final para Ollama: {}", finalPrompt.substring(0, Math.min(100, finalPrompt.length())));

        MaskingResult maskingResult = DataMasker.maskSensitiveData(question);
        String maskedQuestion = maskingResult.getMaskedText();
        long total = maskingResult.getTotal();

        try {
            String answer = ollamaChatModel.call(finalPrompt) + "\n";
            questionService.saveQuestion(maskedQuestion, answer, "ollama", chatId);

            if (total > 0) {
                answer += (total > 1 ? "Foram encontrados " : "Foi encontrado ")
                        + total + " dado" + (total > 1 ? "s " : " ")
                        + (total > 1 ? "sensíveis" : "sensível")
                        + " na tua mensagem. Os dados foram mascarados por questões de segurança.\n";
            }

            // log.info("Resposta Ollama gerada com sucesso");
            return answer;
        } catch (Exception e) {
            log.error("Erro ao chamar Ollama: {}", e.getMessage(), e);
            return "Erro ao processar a pergunta: " + e.getMessage();
        }
    }

    private String buildChatContext(Long chatId) {
        if (chatId == null) {
            // log.info("ChatId é null, retornando contexto vazio");
            return "";
        }

        try {
            List<Question> history = questionService.getQuestionsByChat(chatId);
            // log.info("Carregando histórico do chat {} - {} mensagens encontradas", chatId, history.size());
            
            if (history.isEmpty()) {
                return "";
            }

            StringBuilder context = new StringBuilder();
            for (Question q : history) {
                context.append("Usuário: ").append(q.getQuestion()).append("\n");
                context.append(q.getModel()).append(": ").append(q.getAnswer()).append("\n");
            }
            
            log.info("Contexto construído com {} caracteres", context.length());
            return context.toString();
        } catch (Exception e) {
            log.error("Erro ao carregar histórico do chat {}: {}", chatId, e.getMessage(), e);
            return "";
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElseThrow();
    }

    public ResponseEntity<QuestionYearMonthRequest> findByYearAndMonth(Integer year, Integer month) {
        LocalDateTime fromDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime toDate = fromDate.withDayOfMonth(fromDate.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59);

        List<Question> questions = questionRepo.findByDateBetween(fromDate, toDate);

        List<QuestionDTO> dtos = questions.stream().map(q -> {
            QuestionDTO dto = new QuestionDTO();
            dto.setQuestion(q.getQuestion());
            dto.setAnswer(q.getAnswer());
            dto.setModel(q.getModel());
            dto.setDate(q.getDate());
            dto.setUsername(q.getUser().getUsername());
            return dto;
        }).toList();

        QuestionYearMonthRequest response = new QuestionYearMonthRequest();
        response.setQuestions(dtos);
        response.setYear(year);
        response.setMonth(month);

        return ResponseEntity.ok(response);
    }
}
