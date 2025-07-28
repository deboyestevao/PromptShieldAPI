package com.example.PromptShieldAPI.implementations;

import com.example.PromptShieldAPI.interfaces.QuestionService;
import com.example.PromptShieldAPI.model.Question;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.QuestionRepository;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.repository.ChatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import com.example.PromptShieldAPI.model.Chat;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final ChatRepository chatRepo;

    @Transactional
    public void saveQuestion(String question, String answer, String model, Long chatId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();

        Question q = new Question();
        q.setQuestion(question);
        q.setAnswer(answer);
        q.setModel(model);
        q.setUser(user);
        if (chatId != null) {
            Chat chat = chatRepo.findById(chatId).orElse(null);
            q.setChat(chat);
        }
        questionRepo.save(q);
    }

    public List<Question> getQuestionsByChat(Long chatId) {
        return questionRepo.findByChatIdOrderByDateAsc(chatId);
    }
}
