package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.model.Chat;
import com.example.PromptShieldAPI.model.Question;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.ChatRepository;
import com.example.PromptShieldAPI.repository.QuestionRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.interfaces.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatRepository chatRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionRepository questionRepo;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepo.findByUsername(username).orElse(null);
            
            Map<String, Object> response = Map.of(
                "status", "OK",
                "username", username,
                "userFound", user != null,
                "userId", user != null ? user.getId() : null,
                "message", "Sistema funcionando corretamente"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", "Erro no sistema: " + e.getMessage()
            ));
        }
    }

    @GetMapping("")
    public List<Chat> listChats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        return chatRepo.findActiveByUser(user);
    }

    @PostMapping("")
    public Chat createChat(@RequestBody Chat chat) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        chat.setUser(user);
        return chatRepo.save(chat);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateChat(@PathVariable Long id, @RequestBody Chat chatUpdate) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepo.findByUsername(username).orElseThrow();
            Chat chat = chatRepo.findActiveById(id).orElse(null);
            
            if (chat == null || !chat.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Chat não encontrado ou não pertence ao usuário.");
            }
            
            // Atualizar apenas o nome do chat
            if (chatUpdate.getName() != null) {
                chat.setName(chatUpdate.getName());
                chatRepo.save(chat);
            }
            
            return ResponseEntity.ok(chat);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao atualizar chat: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable Long id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepo.findByUsername(username).orElseThrow();
            Chat chat = chatRepo.findActiveById(id).orElse(null);
            
            if (chat == null || !chat.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Chat não encontrado ou não pertence ao usuário.");
            }
            
            // Soft delete das perguntas associadas ao chat
            List<Question> questions = questionRepo.findActiveByChatIdOrderByDateAsc(id);
            for (Question question : questions) {
                question.softDelete(username);
                questionRepo.save(question);
            }
            
            // Soft delete do chat
            chat.softDelete(username);
            chatRepo.save(chat);
            
            return ResponseEntity.ok().body(Map.of("message", "Chat movido para a lixeira"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao deletar chat: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<?> restoreChat(@PathVariable Long id) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepo.findByUsername(username).orElseThrow();
            
            // Buscar chat deletado
            Chat chat = chatRepo.findById(id).orElse(null);
            if (chat == null || !chat.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Chat não encontrado ou não pertence ao usuário.");
            }
            
            if (!chat.isDeleted()) {
                return ResponseEntity.badRequest().body("Chat não está deletado.");
            }
            
            // Restaurar perguntas do chat
            List<Question> questions = questionRepo.findDeletedByChatId(id);
            for (Question question : questions) {
                question.restore();
                questionRepo.save(question);
            }
            
            // Restaurar chat
            chat.restore();
            chatRepo.save(chat);
            
            return ResponseEntity.ok().body(Map.of("message", "Chat restaurado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao restaurar chat: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/questions")
    public List<Question> getChatQuestions(@PathVariable Long id) {
        // Segurança: só retorna se o chat for do usuário
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        Chat chat = chatRepo.findActiveById(id).orElse(null);
        if (chat == null || !chat.getUser().getId().equals(user.getId())) {
            return List.of();
        }
        return questionService.getQuestionsByChat(id);
    }

    @GetMapping("/deleted")
    public ResponseEntity<?> getDeletedChats() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepo.findByUsername(username).orElseThrow();
            
            // Verificar se é admin (pode ser implementado com roles)
            // Por enquanto, permitir que qualquer usuário veja seus próprios chats deletados
            List<Chat> deletedChats = chatRepo.findDeletedByUser(user);
            
            return ResponseEntity.ok(deletedChats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao buscar chats deletados: " + e.getMessage()));
        }
    }
}