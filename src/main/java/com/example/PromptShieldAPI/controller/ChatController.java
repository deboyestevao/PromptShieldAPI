package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.model.Chat;
import com.example.PromptShieldAPI.model.Question;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.ChatRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.interfaces.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatRepository chatRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private QuestionService questionService;

    @GetMapping("")
    public List<Chat> listChats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        return chatRepo.findByUser(user);
    }

    @PostMapping("")
    public Chat createChat(@RequestBody Chat chat) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        chat.setUser(user);
        return chatRepo.save(chat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        Chat chat = chatRepo.findById(id).orElse(null);
        if (chat == null || !chat.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Chat não encontrado ou não pertence ao usuário.");
        }
        chatRepo.delete(chat);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/questions")
    public List<Question> getChatQuestions(@PathVariable Long id) {
        // Segurança: só retorna se o chat for do usuário
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        Chat chat = chatRepo.findById(id).orElse(null);
        if (chat == null || !chat.getUser().getId().equals(user.getId())) {
            return List.of();
        }
        return questionService.getQuestionsByChat(id);
    }
}