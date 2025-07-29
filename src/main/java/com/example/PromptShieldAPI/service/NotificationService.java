package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.model.Notification;
import com.example.PromptShieldAPI.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Criar nova notificação
    public Notification createNotification(Notification.NotificationType type, String title, String message, String actionUrl) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    // Buscar notificações não lidas
    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadOrderByCreatedAtDesc(false);
    }

    // Buscar notificações recentes
    public List<Notification> getRecentNotifications() {
        List<Notification> allNotifications = notificationRepository.findRecentNotifications();
        return allNotifications.stream().limit(10).toList();
    }

    // Contar notificações não lidas
    public long getUnreadCount() {
        return notificationRepository.countByRead(false);
    }

    // Marcar notificação como lida
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    // Marcar todas como lidas
    public void markAllAsRead() {
        List<Notification> unreadNotifications = getUnreadNotifications();
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    // Criar notificação de report pendente
    public void createReportNotification(String userName) {
        createNotification(
            Notification.NotificationType.REPORT,
            "Novo Report Pendente",
            "Utilizador '" + userName + "' solicitou reativação de conta",
            "/admin/reports"
        );
    }

    // Criar notificação de alteração de sistema
    public void createSystemChangeNotification(String modelName, String action, String adminName) {
        createNotification(
            Notification.NotificationType.SYSTEM,
            "Alteração no Sistema",
            "Modelo " + modelName + " foi " + action + " por " + adminName,
            "/admin/system-preferences"
        );
    }

    // Criar notificação de alerta
    public void createAlertNotification(String title, String message) {
        createNotification(
            Notification.NotificationType.ALERT,
            title,
            message,
            "/admin/dashboard"
        );
    }
} 