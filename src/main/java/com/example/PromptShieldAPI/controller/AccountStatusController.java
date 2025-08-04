package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.model.AccountReport;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.repository.AccountReportRepository;
import com.example.PromptShieldAPI.service.NotificationService;
import com.example.PromptShieldAPI.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountStatusController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountReportRepository accountReportRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/check-account-status")
    public String checkAccountStatus() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null) {
            // Atualizar last_login_at
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            
            // Verificar se está deletado
            if (user.isDeleted()) {
                return "redirect:/account-deleted";
            }
            
            // Verificar se está ativo
            if (!user.isActive()) {
                return "redirect:/account-disabled";
            }
        }
        
        return "redirect:/chat";
    }

    @GetMapping("/account-disabled")
    public String accountDisabledPage() {
        return "accountDisabled";
    }

    @GetMapping("/account-deleted")
    public String accountDeletedPage() {
        return "accountDeleted";
    }

    @PostMapping("/report-account-issue")
    public String reportAccountIssue(@RequestParam String reason) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null) {
            AccountReport report = new AccountReport();
            report.setUser(user);
            report.setReason(reason);
            accountReportRepository.save(report);
            
            // Criar notificação para o admin
            notificationService.createReportNotification(user.getUsername());
            
            // Registrar atividade no log
            activityLogService.logActivity(
                "REPORT_CREATED",
                "Novo Report Criado",
                "Utilizador '" + username + "' criou um report de problema de conta",
                username
            );
        }
        
        return "redirect:/account-disabled?reported=true";
    }
} 