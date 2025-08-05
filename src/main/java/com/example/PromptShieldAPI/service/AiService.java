package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.dto.QuestionDTO;
import com.example.PromptShieldAPI.dto.QuestionYearMonthRequest;
import com.example.PromptShieldAPI.interfaces.QuestionService;
import com.example.PromptShieldAPI.model.Chat;
import com.example.PromptShieldAPI.model.Question;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.ChatRepository;
import com.example.PromptShieldAPI.repository.QuestionRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.service.MaskingResult;
import com.example.PromptShieldAPI.util.DataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
    private final ChatRepository chatRepo;


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

        // Verificar se é a primeira pergunta do chat e gerar nome
        if (chatId != null) {
            List<Question> history = questionService.getQuestionsByChat(chatId);
            if (history.isEmpty()) {
                log.info("Primeira pergunta detectada para chat ID: {}", chatId);
                // É a primeira pergunta, gerar nome do chat
                String chatName = generateChatName(question);
                // Atualizar o nome do chat
                try {
                    Chat chat = chatRepo.findById(chatId).orElse(null);
                    if (chat != null && (chat.getName() == null || chat.getName().trim().isEmpty())) {
                        log.info("Atualizando nome do chat de '{}' para '{}'", chat.getName(), chatName);
                        chat.setName(chatName);
                        chatRepo.save(chat);
                        log.info("Nome do chat atualizado com sucesso");
                    } else {
                        log.info("Chat não encontrado ou já tem nome: {}", chat != null ? chat.getName() : "null");
                    }
                } catch (Exception e) {
                    log.error("Erro ao atualizar nome do chat: {}", e.getMessage());
                }
            } else {
                log.info("Não é primeira pergunta, histórico tem {} perguntas", history.size());
            }
        }
        
        // Carregar histórico do chat para contexto
        String context = buildChatContext(chatId);
        
        // Aplica mascaramento à pergunta antes de enviar para a IA
        MaskingResult maskingResult = DataMasker.maskSensitiveData(question);
        String maskedQuestion = maskingResult.getMaskedText();
        
        String finalPrompt = context + "Usuário: " + maskedQuestion + "\nOpenAI: ";
        
        // log.info("Prompt final para OpenAI: {}", finalPrompt.substring(0, Math.min(100, finalPrompt.length())));

        try {
            String answer = chatModel.call(finalPrompt) + "\n";
            // Mascara a resposta da IA antes de guardar na base de dados
            MaskingResult maskedAnswer = DataMasker.maskSensitiveData(answer);
            String maskedAnswerText = maskedAnswer.getMaskedText();
            
            // Salva a pergunta e resposta mascaradas na base de dados para segurança
            questionService.saveQuestion(maskedQuestion, maskedAnswerText, "openai", chatId);

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

        // Verificar se é a primeira pergunta do chat e gerar nome
        if (chatId != null) {
            List<Question> history = questionService.getQuestionsByChat(chatId);
            if (history.isEmpty()) {
                log.info("Primeira pergunta detectada para chat ID: {}", chatId);
                // É a primeira pergunta, gerar nome do chat
                String chatName = generateChatName(question);
                // Atualizar o nome do chat
                try {
                    Chat chat = chatRepo.findById(chatId).orElse(null);
                    if (chat != null && (chat.getName() == null || chat.getName().trim().isEmpty())) {
                        log.info("Atualizando nome do chat de '{}' para '{}'", chat.getName(), chatName);
                        chat.setName(chatName);
                        chatRepo.save(chat);
                        log.info("Nome do chat atualizado com sucesso");
                    } else {
                        log.info("Chat não encontrado ou já tem nome: {}", chat != null ? chat.getName() : "null");
                    }
                } catch (Exception e) {
                    log.error("Erro ao atualizar nome do chat: {}", e.getMessage());
                }
            } else {
                log.info("Não é primeira pergunta, histórico tem {} perguntas", history.size());
            }
        }

        // Carregar histórico do chat para contexto
        String context = buildChatContext(chatId);
        
        // Aplica mascaramento à pergunta antes de enviar para a IA
        MaskingResult maskingResult = DataMasker.maskSensitiveData(question);
        String maskedQuestion = maskingResult.getMaskedText();
        
        String finalPrompt = context + "Usuário: " + maskedQuestion + "\nOllama: ";
        
        // log.info("Prompt final para Ollama: {}", finalPrompt.substring(0, Math.min(100, finalPrompt.length())));

        try {
            String answer = ollamaChatModel.call(finalPrompt) + "\n";
            // Mascara a resposta da IA antes de guardar na base de dados
            MaskingResult maskedAnswer = DataMasker.maskSensitiveData(answer);
            String maskedAnswerText = maskedAnswer.getMaskedText();
            
            // Salva a pergunta e resposta mascaradas na base de dados para segurança
            questionService.saveQuestion(maskedQuestion, maskedAnswerText, "ollama", chatId);

            // log.info("Resposta Ollama gerada com sucesso");
            return answer;
        } catch (Exception e) {
            log.error("Erro ao chamar Ollama: {}", e.getMessage(), e);
            return "Erro ao processar a pergunta: " + e.getMessage();
        }
    }

    private String buildChatContext(Long chatId) {
        StringBuilder context = new StringBuilder();
        
        // Carrega o prompt do sistema do arquivo de texto
        try {
            String systemPrompt = new String(getClass().getResourceAsStream("/system-prompt.config").readAllBytes());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy, HH:mm", new Locale("pt", "PT"));
            String dataFormatada = LocalDateTime.now().format(formatter);

            context.append("\nDIA DA SEMANA, DIA E HORA ATUAL: ").append(dataFormatada)
                    .append(systemPrompt)
                    .append("\nNOME DO USER: ")
                    .append(getCurrentUser().getFirstName())
                    .append("\nROLE:")
                    .append(getCurrentUser().getRole())
                    .append("\n\n=== HISTÓRICO DA CONVERSA ===\n");
        } catch (Exception e) {
            log.error("Erro ao carregar prompt do sistema: {}", e.getMessage());
            context.append("Erro ao carregar configurações do sistema.\n\n=== HISTÓRICO DA CONVERSA ===\n");
        }
        
        if (chatId == null) {
            return context.toString();
        }

        try {
            List<Question> history = questionService.getQuestionsByChat(chatId);
            
            if (history.isEmpty()) {
                context.append("Nenhuma conversa anterior.\n");
                return context.toString();
            }

            for (Question q : history) {
                context.append("Usuário: ").append(q.getQuestion()).append("\n");
                context.append(q.getModel()).append(": ").append(q.getAnswer()).append("\n");
            }
            
            log.info("Contexto construído com {} caracteres", context.length());
            return context.toString();
        } catch (Exception e) {
            log.error("Erro ao carregar histórico do chat {}: {}", chatId, e.getMessage(), e);
            context.append("Erro ao carregar histórico da conversa.\n");
            return context.toString();
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElseThrow();
    }

    private String generateChatName(String question) {
        try {
            String prompt = "Cria um título curto e descritivo (máximo 30 caracteres) que explique o tema da pergunta por poucas palavras. NÃO uses dois pontos (:). Exemplos: 'Investimento Ações', 'Código Python', 'Restaurantes Lisboa', 'Backup Dados'. Responde APENAS com o título:\n\n" + question;
            
            log.info("Gerando nome para chat com pergunta: {}", question);
            String chatName = chatModel.call(prompt).trim();
            log.info("Resposta da IA para nome do chat: '{}'", chatName);
            
            // Limitar a 30 caracteres e remover aspas se existirem
            chatName = chatName.replaceAll("[\"']", "").trim();
            if (chatName.length() > 30) {
                chatName = chatName.substring(0, 30);
            }
            
            String finalName = chatName.isEmpty() ? "Nova conversa" : chatName;
            log.info("Nome final do chat: '{}'", finalName);
            
            return finalName;
        } catch (Exception e) {
            log.error("Erro ao gerar nome do chat: {}", e.getMessage());
            return "Nova conversa";
        }
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
