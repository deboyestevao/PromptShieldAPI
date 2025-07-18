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
    public String ask(@RequestBody QuestionWithFilesRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        // ⛑️ Verifica disponibilidade real e atualiza o estado no banco
        configService.checkAndUpdateModelStatus(ModelType.OPENAI);
        configService.checkAndUpdateModelStatus(ModelType.OLLAMA);

        // ✅ Usa status atualizado direto do banco (via isModelEnabled)
        boolean useOpenAi = configService.isModelEnabled(ModelType.OPENAI);
        boolean useOllama = configService.isModelEnabled(ModelType.OLLAMA);

        if (!useOpenAi && !useOllama) {
            return "Nenhum LLM está ativado na configuração do sistema.";
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
            return "O conteúdo total ultrapassa o limite de tokens permitido (4096). Reduza os ficheiros ou a pergunta e tente novamente.";
        }

        // 🤖 Faz pergunta ao(s) modelo(s) ativo(s)
        StringBuilder answer = new StringBuilder();

        if (useOpenAi) {
            String a = aiService.askOpenAi(finalPrompt);
            answer.append("OpenAI: ").append(a).append("\n");
        }

        if (useOllama) {
            String a = aiService.askOllama(finalPrompt);
            answer.append("Ollama: ").append(a).append("\n");
        }

        return answer.toString();
    }

    @GetMapping("/find-year-month")
    public ResponseEntity<QuestionYearMonthRequest> findByDate(@RequestParam Integer year, Integer month) {
        return aiService.findByYearAndMonth(year, month);
    }
}
