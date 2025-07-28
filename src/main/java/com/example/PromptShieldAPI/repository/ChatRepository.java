package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.Chat;
import com.example.PromptShieldAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUser(User user);
}