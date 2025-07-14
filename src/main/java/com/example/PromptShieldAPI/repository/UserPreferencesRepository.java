package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    Optional<UserPreferences> findByUser(User user);
}
