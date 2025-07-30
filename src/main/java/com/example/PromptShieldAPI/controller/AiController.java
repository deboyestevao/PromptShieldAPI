package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.QuestionWithFilesRequest;
import com.example.PromptShieldAPI.dto.QuestionYearMonthRequest;
import com.example.PromptShieldAPI.service.*;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import com.example.PromptShieldAPI.model.Chat;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.ChatRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.PromptShieldAPI.util.DataMasker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;
    private final SystemConfigService configService;
    private final FileService fileService;
    private final ChatRepository chatRepository;

    private final UserRepository userRepository;

    @GetMapping("/welcome")
    public String welcome() {
        String user  = SecurityContextHolder.getContext().getAuthentication().getName();
        return "Bem vindo/a " + user;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody QuestionWithFilesRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long chatId = request.getChatId();

        // 🔒 Validação de segurança do chat
        if (chatId != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuário não encontrado"));
            }
            
            Chat chat = chatRepository.findById(chatId).orElse(null);
            if (chat == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Chat não encontrado"));
            }
            
            if (!chat.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Acesso negado ao chat"));
            }
        }

        // ⛑️ Verifica disponibilidade real e atualiza o estado no banco
        configService.checkAndUpdateModelStatus(ModelType.OPENAI);
        configService.checkAndUpdateModelStatus(ModelType.OLLAMA);

        // ✅ Usa status atualizado direto do banco (via isModelEnabled)
        boolean useOpenAi = configService.isModelEnabled(ModelType.OPENAI);
        boolean useOllama = configService.isModelEnabled(ModelType.OLLAMA);

        if (!useOpenAi && !useOllama) {
            return ResponseEntity.ok().body(Map.of(
                "maskedQuestion", "",
                "llmAnswers", List.of("Nenhum LLM está ativado na configuração do sistema.")
            ));
        }

        // 📁 Carrega contexto de ficheiros
        String fileContext = "";
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            fileContext = fileService.loadFilesContent(username, request.getFileIds());
        }

        String question = request.getQuestion();
        String finalPrompt = fileContext.isBlank() ? question : fileContext + "\n\nPergunta: " + question;

        int tokenEstimate = fileService.estimateTokens(finalPrompt);
        if (tokenEstimate > 4096) {
            return ResponseEntity.ok().body(Map.of(
                "maskedQuestion", "",
                "llmAnswers", List.of("O conteúdo total ultrapassa o limite de tokens permitido (4096). Reduza os ficheiros ou a pergunta e tente novamente.")
            ));
        }

        // Aplica DataMasker à pergunta do utilizador (sem conteúdo de ficheiros)
        MaskingResult maskingResult = DataMasker.maskSensitiveData(question);
        String maskedQuestion = maskingResult.getMaskedText();
        
        // 🤖 Faz pergunta ao(s) modelo(s) ativo(s)
        List<String> llmAnswers = new ArrayList<>();

        try {
            if (useOpenAi) {
                String a = aiService.askOpenAi(finalPrompt, chatId);
                log.info("Resposta OpenAI recebida - Tamanho: {} caracteres", a.length());
                llmAnswers.add("OpenAI: " + a);
            }

            if (useOllama) {
                String a = aiService.askOllama(finalPrompt, chatId);
                log.info("Resposta Ollama recebida - Tamanho: {} caracteres", a.length());
                llmAnswers.add("Ollama: " + a);
            }
        } catch (Exception e) {
            log.error("Erro ao processar pergunta: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Erro ao processar a pergunta: " + e.getMessage()
            ));
        }

        Map<String, Object> response = Map.of(
            "maskedQuestion", maskedQuestion,
            "llmAnswers", llmAnswers,
            "attachedFiles", request.getFileIds() != null ? request.getFileIds().size() : 0
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/find-year-month")
    public ResponseEntity<QuestionYearMonthRequest> findByDate(@RequestParam Integer year, Integer month) {
        return aiService.findByYearAndMonth(year, month);
    }

    @GetMapping("/role")
    public String getRole() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return "";
        return auth.getAuthorities().stream()
            .map(a -> a.getAuthority().replace("ROLE_", "").toLowerCase())
            .findFirst().orElse("");
    }


}
