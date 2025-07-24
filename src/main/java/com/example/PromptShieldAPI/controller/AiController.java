package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.QuestionWithFilesRequest;
import com.example.PromptShieldAPI.dto.QuestionYearMonthRequest;
import com.example.PromptShieldAPI.service.AiService;
import com.example.PromptShieldAPI.service.FileService;
import com.example.PromptShieldAPI.service.SystemConfigService;
import com.example.PromptShieldAPI.model.SystemConfig.ModelType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.PromptShieldAPI.util.DataMasker;
import com.example.PromptShieldAPI.service.MaskingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final SystemConfigService configService;
    private final FileService fileService;

    @GetMapping("/welcome")
    public String welcome() {
        String user  = SecurityContextHolder.getContext().getAuthentication().getName();
        return "Bem vindo/a " + user;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody QuestionWithFilesRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

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
            fileContext = fileService.loadFilesContent(userId, request.getFileIds());
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

        // Aplica DataMasker à pergunta do utilizador
        MaskingResult maskingResult = DataMasker.maskSensitiveData(question);
        String maskedQuestion = maskingResult.getMaskedText();

        // 🤖 Faz pergunta ao(s) modelo(s) ativo(s)
        List<String> llmAnswers = new ArrayList<>();

        if (useOpenAi) {
            String a = aiService.askOpenAi(finalPrompt);
            llmAnswers.add("OpenAI: " + a);
        }

        if (useOllama) {
            String a = aiService.askOllama(finalPrompt);
            llmAnswers.add("Ollama: " + a);
        }

        Map<String, Object> response = Map.of(
            "maskedQuestion", maskedQuestion,
            "llmAnswers", llmAnswers
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
