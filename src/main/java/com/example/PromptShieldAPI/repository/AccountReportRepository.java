package com.example.PromptShieldAPI.repository;

import com.example.PromptShieldAPI.model.AccountReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountReportRepository extends JpaRepository<AccountReport, Long> {
    
    @Query("SELECT ar FROM AccountReport ar WHERE ar.status = 'PENDING' ORDER BY ar.createdAt DESC")
    List<AccountReport> findPendingReports();
    
    @Query("SELECT ar FROM AccountReport ar ORDER BY ar.createdAt DESC")
    List<AccountReport> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(ar) FROM AccountReport ar WHERE ar.status = 'PENDING'")
    long countPendingReports();
    
    List<AccountReport> findTop5ByOrderByCreatedAtDesc();
} 