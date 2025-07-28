package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.ConfigHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigHistoryRepository extends JpaRepository<ConfigHistory, Long> {
}
