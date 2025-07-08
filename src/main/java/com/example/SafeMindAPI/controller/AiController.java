package com.example.SafeMindAPI.controller;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.Post;
import com.example.SafeMindAPI.dto.QuestionWithFilesRequest;
import com.example.SafeMindAPI.dto.QuestionYearMonthRequest;
import com.example.SafeMindAPI.model.Question;
import com.example.SafeMindAPI.repository.SystemConfigRepository;
import com.example.SafeMindAPI.repository.UserRepository;
import com.example.SafeMindAPI.service.AiService;
import com.example.SafeMindAPI.service.FileService;
import com.example.SafeMindAPI.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.geom.QuadCurve2D;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

        boolean useOpenAi = configService.isOpenAiEnabled();
        boolean useOllama = configService.isOllamaEnabled();

        String fileContext = "";
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            fileContext = fileService.loadFilesContent(userId, request.getFileIds());
        }

        String question = request.getQuestion();
        String finalPrompt = fileContext.isBlank() ? question : fileContext + "\n\nPergunta: " + question;

        int tokenEstimate = fileService.estimateTokens(finalPrompt);
        if (tokenEstimate > 4096) {
            return "O conteúdo total ultrapassa o limite de tokens permitido 4096. Reduza os ficheiros ou a pergunta e tente novamente.";
        }

        StringBuilder answer = new StringBuilder();

        if (useOpenAi) {
            String a = aiService.askOpenAi(finalPrompt);
            answer.append("OpenAi: ").append(a);
        }

        if (useOllama) {
            String a = aiService.askOllama(finalPrompt);
            answer.append("Ollama : ").append(a);
        }

        if (!useOpenAi && !useOllama) {
            return "Nenhum LLM está ativado na configuração do sistema.";
        }

        return answer.toString();
    }

    @GetMapping("/find-year-month")
    public ResponseEntity<QuestionYearMonthRequest> findByDate(@RequestParam Integer year, Integer month) {
        return aiService.findByYearAndMonth(year, month);
    }
}
