package com.example.SafeMindAPI.repository;

import com.example.SafeMindAPI.model.SystemConfig;
import com.example.SafeMindAPI.model.SystemConfig.ModelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    Optional<SystemConfig> findByModel(ModelType model);
}
