package com.example.PromptShieldAPI.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String message;
    private boolean read = false;
    private String actionUrl; // URL para onde ir quando clicar

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum NotificationType {
        REPORT("report", "Relatório"),
        SYSTEM("settings", "Sistema"),
        USER("users", "Utilizador"),
        ALERT("alert-triangle", "Alerta"),
        STATS("bar-chart", "Estatística");

        private final String icon;
        private final String displayName;

        NotificationType(String icon, String displayName) {
            this.icon = icon;
            this.displayName = displayName;
        }

        public String getIcon() { return icon; }
        public String getDisplayName() { return displayName; }
    }
} 