package com.example.PromptShieldAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String name;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by")
    private String deletedBy;
    
    // Método para soft delete
    public void softDelete(String username) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = username;
    }
    
    // Método para verificar se está deletado
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    // Método para restaurar
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
} 