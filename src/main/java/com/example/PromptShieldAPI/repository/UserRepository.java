package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // ainda pode ser útil noutros contextos
    Optional<User> findByEmail(String email);       // necessário para login por email
    
    // Métodos para dashboard
    long countByIsOnlineTrue();
    long countByActiveTrue();
    long countByActiveFalse();
    List<User> findTop5ByOrderByLastLoginAtDesc();
    
    // Alternative method for Boolean field
    @Query("SELECT COUNT(u) FROM User u WHERE u.isOnline = true")
    long countOnlineUsers();
}
