package com.example.PromptShieldAPI.implementations;

import com.example.PromptShieldAPI.interfaces.QuestionService;
import com.example.PromptShieldAPI.model.Question;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.QuestionRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.repository.ChatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import com.example.PromptShieldAPI.model.Chat;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final ChatRepository chatRepo;

    @Transactional
    public void saveQuestion(String question, String answer, String model, Long chatId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepo.findByUsername(username).orElseThrow();
            
            // log.info("Salvando pergunta - chatId: {}, model: {}, user: {}", chatId, model, username);

            Question q = new Question();
            q.setQuestion(question);
            q.setAnswer(answer);
            q.setModel(model);
            q.setUser(user);
            
            if (chatId != null) {
                Chat chat = chatRepo.findById(chatId).orElse(null);
                if (chat != null) {
                    q.setChat(chat);
                    // log.info("Chat encontrado e associado: {}", chatId);
                } else {
                    // log.warn("Chat não encontrado para ID: {}", chatId);
                }
            } else {
                // log.info("ChatId é null, salvando pergunta sem chat");
            }
            
            questionRepo.save(q);
            // log.info("Pergunta salva com sucesso - ID: {}", q.getId());
        } catch (Exception e) {
            log.error("Erro ao salvar pergunta: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<Question> getQuestionsByChat(Long chatId) {
        try {
            // log.info("Buscando perguntas do chat: {}", chatId);
            List<Question> questions = questionRepo.findByChatIdOrderByDateAsc(chatId);
            // log.info("Encontradas {} perguntas para o chat {}", questions.size(), chatId);
            return questions;
        } catch (Exception e) {
            log.error("Erro ao buscar perguntas do chat {}: {}", chatId, e.getMessage(), e);
            return List.of();
        }
    }
}
