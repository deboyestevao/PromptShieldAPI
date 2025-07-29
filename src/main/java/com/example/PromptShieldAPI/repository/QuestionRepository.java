package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.Question;
import com.example.PromptShieldAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    // Buscar perguntas ativas por usuário
    @Query("SELECT q FROM Question q WHERE q.user = :user AND q.deletedAt IS NULL ORDER BY q.date DESC")
    List<Question> findActiveByUser(@Param("user") User user);
    
    // Buscar perguntas ativas por chat
    @Query("SELECT q FROM Question q WHERE q.chat.id = :chatId AND q.deletedAt IS NULL ORDER BY q.date ASC")
    List<Question> findActiveByChatIdOrderByDateAsc(@Param("chatId") Long chatId);
    
    // Buscar perguntas por período (apenas ativas)
    @Query("SELECT q FROM Question q WHERE q.date BETWEEN :fromDate AND :toDate AND q.deletedAt IS NULL ORDER BY q.date DESC")
    List<Question> findActiveByDateBetween(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
    
    // Buscar todas as perguntas deletadas de um chat (para soft delete em cascata)
    @Query("SELECT q FROM Question q WHERE q.chat.id = :chatId AND q.deletedAt IS NOT NULL")
    List<Question> findDeletedByChatId(@Param("chatId") Long chatId);
    
    // Métodos legacy para compatibilidade
    default Optional<Question> findByUser(User user) {
        List<Question> questions = findActiveByUser(user);
        return questions.isEmpty() ? Optional.empty() : Optional.of(questions.get(0));
    }
    
    default List<Question> findByDateBetween(LocalDateTime fromDate, LocalDateTime toDate) {
        return findActiveByDateBetween(fromDate, toDate);
    }
    
    default List<Question> findByChatIdOrderByDateAsc(Long chatId) {
        return findActiveByChatIdOrderByDateAsc(chatId);
    }
}
