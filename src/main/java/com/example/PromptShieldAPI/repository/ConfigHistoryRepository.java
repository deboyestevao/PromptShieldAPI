package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.ConfigHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConfigHistoryRepository extends JpaRepository<ConfigHistory, Long> {
    List<ConfigHistory> findAllByOrderByChangedAtDesc();
}
