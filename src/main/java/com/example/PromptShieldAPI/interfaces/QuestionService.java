package com.example.PromptShieldAPI.interfaces;

import com.example.PromptShieldAPI.model.Question;

import java.util.List;

public interface QuestionService {
    void saveQuestion(String question, String answer, String model, Long chatId);
    List<Question> getQuestionsByChat(Long chatId);
}
