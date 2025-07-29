package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Buscar notificações não lidas
    List<Notification> findByReadOrderByCreatedAtDesc(boolean read);
    
    // Contar notificações não lidas
    long countByRead(boolean read);
    
    // Buscar notificações recentes (últimas 10)
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications();
    
    // Marcar todas como lidas
    @Query("UPDATE Notification n SET n.read = true WHERE n.read = false")
    void markAllAsRead();
} 