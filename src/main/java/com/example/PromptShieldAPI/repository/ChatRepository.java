package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.Chat;
import com.example.PromptShieldAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    // Buscar apenas chats ativos (não deletados) do usuário
    @Query("SELECT c FROM Chat c WHERE c.user = :user AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Chat> findActiveByUser(@Param("user") User user);
    
    // Buscar chat por ID apenas se estiver ativo
    @Query("SELECT c FROM Chat c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Chat> findActiveById(@Param("id") Long id);
    
    // Buscar todos os chats deletados do usuário (para admin)
    @Query("SELECT c FROM Chat c WHERE c.user = :user AND c.deletedAt IS NOT NULL ORDER BY c.deletedAt DESC")
    List<Chat> findDeletedByUser(@Param("user") User user);
    
    // Contar chats ativos de um utilizador
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.user = :user AND c.deletedAt IS NULL")
    long countByUser(@Param("user") User user);
    
    // Método legacy para compatibilidade (mantém comportamento antigo)
    default List<Chat> findByUser(User user) {
        return findActiveByUser(user);
    }
}