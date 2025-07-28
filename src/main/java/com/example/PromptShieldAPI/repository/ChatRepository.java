package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.Chat;
import com.example.PromptShieldAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUser(User user);
    
    @Query("SELECT COALESCE(MAX(c.userChatNumber), 0) FROM Chat c WHERE c.user = :user")
    Integer findMaxUserChatNumberByUser(@Param("user") User user);
}