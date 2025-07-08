package com.example.SafeMindAPI.implementations;

import com.example.SafeMindAPI.interfaces.QuestionService;
import com.example.SafeMindAPI.model.Question;
import com.example.SafeMindAPI.model.User;
import com.example.SafeMindAPI.repository.QuestionRepository;
import com.example.SafeMindAPI.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;

    @Transactional
    public void saveQuestion(String question, String answer, String model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();

        Question q = new Question();
        q.setQuestion(question);
        q.setAnswer(answer);
        q.setModel(model);
        q.setUser(user);

        questionRepo.save(q);
    }
}
