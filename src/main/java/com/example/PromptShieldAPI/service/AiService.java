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
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AzureOpenAiChatModel chatModel;
    private final OllamaChatModel ollamaChatModel;
    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final SystemConfigService systemConfigService;
    private final QuestionService questionService;

    private static final String INPUT_FOLDER = "InputFiles";

    public String askOpenAi(String question, Long chatId) {
        if (!systemConfigService.isOpenAiEnabled()) {
            return "OpenAi está em manutenção.";
        }

        User user = getCurrentUser();
        if (user.getPreferences() == null || !user.getPreferences().isOpenaiPreferred()) {
            return "OpenAi está desativado para este utilizador.";
        }

        // Carregar histórico do chat para contexto
        StringBuilder context = new StringBuilder();
        if (chatId != null) {
            List<Question> history = questionService.getQuestionsByChat(chatId);
            for (Question q : history) {
                context.append("Usuário: ").append(q.getQuestion()).append("\n");
                context.append("LLM: ").append(q.getAnswer()).append("\n");
            }
        }
        context.append("Usuário: ").append(question).append("\nLLM: ");

        MaskingResult maskingResult = DataMasker.maskSensitiveData(question);
        String maskedQuestion = maskingResult.getMaskedText();
        long total = maskingResult.getTotal();

        String answer = chatModel.call(context.toString()) + "\n";
        questionService.saveQuestion(maskedQuestion, answer, "openai", chatId);

        if (total > 0) {
            answer += (total > 1 ? "Foram encontrados " : "Foi encontrado ")
                    + total + " dado" + (total > 1 ? "s " : " ")
                    + (total > 1 ? "sensíveis" : "sensível")
                    + " na tua mensagem. Os dados foram mascarados por questões de segurança.\n";
        }

        return answer;
    }

    public String askOllama(String question, Long chatId) {
        if (!systemConfigService.isOllamaEnabled()) {
            return "Ollama está em manutenção.";
        }

        User user = getCurrentUser();
        if (user.getPreferences() == null || !user.getPreferences().isOllamaPreferred()) {
            return "Ollama está desativado para este utilizador.";
        }

        // Carregar histórico do chat para contexto
        StringBuilder context = new StringBuilder();
        if (chatId != null) {
            List<Question> history = questionService.getQuestionsByChat(chatId);
            for (Question q : history) {
                context.append("Usuário: ").append(q.getQuestion()).append("\n");
                context.append("LLM: ").append(q.getAnswer()).append("\n");
            }
        }
        context.append("Usuário: ").append(question).append("\nLLM: ");

        MaskingResult maskingResult = DataMasker.maskSensitiveData(question);
        String maskedQuestion = maskingResult.getMaskedText();
        long total = maskingResult.getTotal();

        String answer = ollamaChatModel.call(context.toString()) + "\n";
        questionService.saveQuestion(maskedQuestion, answer, "ollama", chatId);

        if (total > 0) {
            answer += (total > 1 ? "Foram encontrados " : "Foi encontrado ")
                    + total + " dado" + (total > 1 ? "s " : " ")
                    + (total > 1 ? "sensíveis" : "sensível")
                    + " na tua mensagem. Os dados foram mascarados por questões de segurança.\n";
        }

        return answer;
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
