package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.dto.SystemPreferencesRequest;
import com.example.PromptShieldAPI.dto.UserPreferencesRequest;
import com.example.PromptShieldAPI.dto.TemporaryDisableRequest;
import com.example.PromptShieldAPI.model.SystemConfig;
import com.example.PromptShieldAPI.model.UserPreferences;
import com.example.PromptShieldAPI.model.AccountReport;
import com.example.PromptShieldAPI.service.AdminService;
import com.example.PromptShieldAPI.service.SystemConfigService;
import com.example.PromptShieldAPI.repository.AccountReportRepository;
import com.example.PromptShieldAPI.repository.ConfigHistoryRepository;
import com.example.PromptShieldAPI.model.ConfigHistory;
import com.example.PromptShieldAPI.model.Notification;
import com.example.PromptShieldAPI.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.repository.ChatRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SystemConfigService systemConfigService;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final AccountReportRepository accountReportRepository;
    private final ConfigHistoryRepository configHistoryRepository;
    private final NotificationService notificationService;

    @PatchMapping("/system-preferences")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSystemPreferences(@RequestBody SystemPreferencesRequest prefs) {
        try {
            SystemConfig updatedConfig = adminService.updateSystemPreferences(prefs.isOpenai(), prefs.isOllama());
            return ResponseEntity.ok(updatedConfig);
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(409).body("Error: System preferences were updated by another user. Please, try again.");
        }
    }

    @PostMapping("/user-preferences")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserPreferences(@RequestBody UserPreferencesRequest prefs) {
        UserPreferences updatedPreferences = adminService.updateUserPreferences(
                prefs.isOllamaPreferred(),
                prefs.isOpenaiPreferred(),
                prefs.getUserId()
        );
        return ResponseEntity.ok(updatedPreferences);
    }

    @PostMapping("/llm-user-prefs")
    public ResponseEntity<?> updateUserLLMPreferences(@RequestBody java.util.Map<String, Boolean> prefs) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        adminService.updateUserLLMPreferences(username, prefs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/llm-user-prefs")
    public ResponseEntity<?> getUserLLMPreferences() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(adminService.getUserLLMPreferences(username));
    }

    @GetMapping("/llm-status")
    public ResponseEntity<?> getLLMStatus() {
        boolean openai = systemConfigService.isModelEnabled(com.example.PromptShieldAPI.model.SystemConfig.ModelType.OPENAI);
        boolean ollama = systemConfigService.isModelEnabled(com.example.PromptShieldAPI.model.SystemConfig.ModelType.OLLAMA);
        
        // Obter informações detalhadas sobre desligamentos temporários
        SystemConfig openaiConfig = systemConfigService.getModelConfig(SystemConfig.ModelType.OPENAI);
        SystemConfig ollamaConfig = systemConfigService.getModelConfig(SystemConfig.ModelType.OLLAMA);
        
        Map<String, Object> response = new HashMap<>();
        response.put("openai", openai);
        response.put("ollama", ollama);
        
        // Informações sobre desligamentos temporários
        Map<String, Object> openaiDetails = new HashMap<>();
        openaiDetails.put("enabled", openai);
        openaiDetails.put("temporaryDisabled", openaiConfig != null && openaiConfig.isTemporaryDisabled());
        if (openaiConfig != null && openaiConfig.isTemporaryDisabled()) {
            openaiDetails.put("temporaryDisabledUntil", openaiConfig.getTemporaryDisabledEnd());
            openaiDetails.put("temporaryDisabledReason", openaiConfig.getTemporaryDisabledReason());
        }
        response.put("openaiDetails", openaiDetails);
        
        Map<String, Object> ollamaDetails = new HashMap<>();
        ollamaDetails.put("enabled", ollama);
        ollamaDetails.put("temporaryDisabled", ollamaConfig != null && ollamaConfig.isTemporaryDisabled());
        if (ollamaConfig != null && ollamaConfig.isTemporaryDisabled()) {
            ollamaDetails.put("temporaryDisabledUntil", ollamaConfig.getTemporaryDisabledEnd());
            ollamaDetails.put("temporaryDisabledReason", ollamaConfig.getTemporaryDisabledReason());
        }
        response.put("ollamaDetails", ollamaDetails);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/llm-status-simple")
    public ResponseEntity<?> getLLMStatusSimple() {
        boolean openai = systemConfigService.isModelEnabled(com.example.PromptShieldAPI.model.SystemConfig.ModelType.OPENAI);
        boolean ollama = systemConfigService.isModelEnabled(com.example.PromptShieldAPI.model.SystemConfig.ModelType.OLLAMA);
        
        Map<String, Object> response = new HashMap<>();
        response.put("openai", openai);
        response.put("ollama", ollama);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/llm-maintenance-status")
    public ResponseEntity<?> getLLMMaintenanceStatus() {
        SystemConfig openaiConfig = systemConfigService.getModelConfig(SystemConfig.ModelType.OPENAI);
        SystemConfig ollamaConfig = systemConfigService.getModelConfig(SystemConfig.ModelType.OLLAMA);
        
        Map<String, Object> response = new HashMap<>();
        
        // Verificar OpenAI
        if (openaiConfig != null && openaiConfig.isTemporaryDisabled()) {
            Map<String, Object> openaiMaintenance = new HashMap<>();
            openaiMaintenance.put("inMaintenance", true);
            openaiMaintenance.put("until", openaiConfig.getTemporaryDisabledEnd());
            openaiMaintenance.put("reason", openaiConfig.getTemporaryDisabledReason());
            response.put("openai", openaiMaintenance);
        } else {
            response.put("openai", Map.of("inMaintenance", false));
        }
        
        // Verificar Ollama
        if (ollamaConfig != null && ollamaConfig.isTemporaryDisabled()) {
            Map<String, Object> ollamaMaintenance = new HashMap<>();
            ollamaMaintenance.put("inMaintenance", true);
            ollamaMaintenance.put("until", ollamaConfig.getTemporaryDisabledEnd());
            ollamaMaintenance.put("reason", ollamaConfig.getTemporaryDisabledReason());
            response.put("ollama", ollamaMaintenance);
        } else {
            response.put("ollama", Map.of("inMaintenance", false));
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/llm/temporary-disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> temporarilyDisableLLM(@RequestBody TemporaryDisableRequest request) {
        try {
            String adminName = SecurityContextHolder.getContext().getAuthentication().getName();
            SystemConfig.ModelType modelType = SystemConfig.ModelType.valueOf(request.getModel().toUpperCase());
            
            systemConfigService.temporarilyDisableModel(
                modelType, 
                request.getDisableUntil(), 
                request.getReason(), 
                adminName
            );
            
            return ResponseEntity.ok(Map.of("message", "Modelo " + request.getModel() + " desligado temporariamente até " + request.getDisableUntil()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Modelo inválido. Use 'OPENAI' ou 'OLLAMA'"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao desligar modelo temporariamente"));
        }
    }

    @PostMapping("/llm/remove-temporary-disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeTemporaryDisable(@RequestBody Map<String, String> request) {
        try {
            String adminName = SecurityContextHolder.getContext().getAuthentication().getName();
            SystemConfig.ModelType modelType = SystemConfig.ModelType.valueOf(request.get("model").toUpperCase());
            
            systemConfigService.removeTemporaryDisable(modelType, adminName);
            
            return ResponseEntity.ok(Map.of("message", "Desligamento temporário removido para o modelo " + request.get("model")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Modelo inválido. Use 'OPENAI' ou 'OLLAMA'"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao remover desligamento temporário"));
        }
    }

    @GetMapping("/api/config-history")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getConfigHistory() {
        try {
            List<ConfigHistory> history = configHistoryRepository.findAllByOrderByChangedAtDesc();
            List<Map<String, Object>> historyData = history.stream().map(entry -> {
                Map<String, Object> entryMap = new HashMap<>();
                entryMap.put("id", entry.getId());
                entryMap.put("model", entry.getModel().name());
                entryMap.put("modelDisplay", entry.getModel() == SystemConfig.ModelType.OPENAI ? "OpenAI" : "Ollama");
                entryMap.put("enabled", entry.isEnabled());
                entryMap.put("changedBy", entry.getChangedBy());
                entryMap.put("changedAt", entry.getChangedAt());
                entryMap.put("action", entry.isEnabled() ? "Ativado" : "Desativado");
                return entryMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(historyData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoints para notificações
    @GetMapping("/api/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getNotifications() {
        try {
            List<Notification> notifications = notificationService.getRecentNotifications();
            List<Map<String, Object>> notificationData = notifications.stream().map(notification -> {
                Map<String, Object> notificationMap = new HashMap<>();
                notificationMap.put("id", notification.getId());
                notificationMap.put("type", notification.getType().name());
                notificationMap.put("icon", notification.getType().getIcon());
                notificationMap.put("title", notification.getTitle());
                notificationMap.put("message", notification.getMessage());
                notificationMap.put("read", notification.isRead());
                notificationMap.put("actionUrl", notification.getActionUrl());
                notificationMap.put("createdAt", notification.getCreatedAt());
                return notificationMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(notificationData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/api/notifications/count")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotificationCount() {
        try {
            long unreadCount = notificationService.getUnreadCount();
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("unreadCount", 0));
        }
    }

    @PostMapping("/api/notifications/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao marcar notificação como lida"));
        }
    }

    @PostMapping("/api/notifications/read-all")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> markAllNotificationsAsRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao marcar notificações como lidas"));
        }
    }

    @GetMapping("/system-preferences")
    @PreAuthorize("hasRole('ADMIN')")
    public String systemPreferencesPage() {
        return "adminSystemPreferences";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "adminDashboard";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String usersPage() {
        return "adminUsers";
    }

    @GetMapping(value = "/config-history", produces = "text/html")
    @PreAuthorize("hasRole('ADMIN')")
    public String configHistoryPage() {
        return "adminConfigHistory";
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String reportsPage() {
        return "adminReports";
    }

    // API endpoints para gestão de utilizadores
    @GetMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> usersWithStats = users.stream()
                .filter(user -> !user.getUsername().equals(currentUsername)) // Filtrar o utilizador atual
                .map(user -> {
                    long chatCount = chatRepository.countByUser(user);
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
                    userMap.put("lastName", user.getLastName() != null ? user.getLastName() : "");
                    userMap.put("email", user.getEmail());
                    userMap.put("username", user.getUsername());
                    userMap.put("active", user.isActive());
                    userMap.put("createdAt", user.getCreatedAt());
                    userMap.put("chatCount", chatCount);
                    userMap.put("role", user.getRole() != null ? user.getRole() : "USER");
                    return userMap;
                }).collect(Collectors.toList());
            
            return ResponseEntity.ok(usersWithStats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            long chatCount = chatRepository.countByUser(user);
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            userData.put("email", user.getEmail());
            userData.put("username", user.getUsername());
            userData.put("active", user.isActive());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("chatCount", chatCount);
            userData.put("lastActivity", user.getLastLoginAt());
            
            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/api/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            user.setActive(true);
            userRepository.save(user);
            
            // Criar notificação de utilizador ativado
            notificationService.createNotification(
                Notification.NotificationType.USER,
                "Utilizador Ativado",
                "Utilizador '" + user.getUsername() + "' foi ativado",
                "/admin/users"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilizador ativado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao ativar utilizador");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/api/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            user.setActive(false);
            userRepository.save(user);
            
            // Criar notificação de utilizador desativado
            notificationService.createNotification(
                Notification.NotificationType.USER,
                "Utilizador Desativado",
                "Utilizador '" + user.getUsername() + "' foi desativado",
                "/admin/users"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilizador desativado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao desativar utilizador");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar se não é o próprio admin
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (user.getUsername().equals(currentUsername)) {
                Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Não pode eliminar a sua própria conta");
            return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String username = user.getUsername();
            userRepository.delete(user);
            
            // Criar notificação de utilizador eliminado
            notificationService.createNotification(
                Notification.NotificationType.USER,
                "Utilizador Eliminado",
                "Utilizador '" + username + "' foi eliminado",
                "/admin/users"
            );
            
            return ResponseEntity.ok(Map.of("message", "Utilizador eliminado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao eliminar utilizador"));
        }
    }

    @PostMapping("/api/users/{id}/make-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> makeUserAdmin(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar se não é o próprio admin
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (user.getUsername().equals(currentUsername)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Não pode alterar a sua própria conta");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            user.setRole("ADMIN");
            userRepository.save(user);
            
            // Criar notificação de utilizador tornado admin
            notificationService.createNotification(
                Notification.NotificationType.USER,
                "Novo Admin",
                "Utilizador '" + user.getUsername() + "' foi tornado administrador",
                "/admin/users"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilizador tornado admin com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao tornar utilizador admin");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/api/users/{id}/remove-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> removeUserAdmin(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar se não é o próprio admin
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (user.getUsername().equals(currentUsername)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Não pode alterar a sua própria conta");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            user.setRole("USER");
            userRepository.save(user);
            
            // Criar notificação de admin removido
            notificationService.createNotification(
                Notification.NotificationType.USER,
                "Admin Removido",
                "Privilégios de admin removidos de '" + user.getUsername() + "'",
                "/admin/users"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Privilégios de admin removidos com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao remover privilégios de admin");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // API endpoints para gestão de reports
    @GetMapping("/api/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllReports() {
        try {
            List<AccountReport> reports = accountReportRepository.findAllOrderByCreatedAtDesc();
            List<Map<String, Object>> reportsData = reports.stream().map(report -> {
                Map<String, Object> reportMap = new HashMap<>();
                reportMap.put("id", report.getId());
                reportMap.put("userId", report.getUser().getId());
                reportMap.put("userName", report.getUser().getFirstName() + " " + report.getUser().getLastName());
                reportMap.put("userEmail", report.getUser().getEmail());
                reportMap.put("reason", report.getReason());
                reportMap.put("status", report.getStatus().name());
                reportMap.put("statusDisplay", report.getStatus().getDisplayName());
                reportMap.put("createdAt", report.getCreatedAt());
                reportMap.put("resolvedAt", report.getResolvedAt());
                reportMap.put("resolvedBy", report.getResolvedBy() != null ? 
                    report.getResolvedBy().getFirstName() + " " + report.getResolvedBy().getLastName() : null);
                return reportMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(reportsData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/api/reports/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> approveReport(@PathVariable Long id) {
        try {
            AccountReport report = accountReportRepository.findById(id).orElse(null);
            if (report == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Ativar o utilizador
            User user = report.getUser();
            user.setActive(true);
            userRepository.save(user);
            
            // Marcar report como aprovado
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User admin = userRepository.findByUsername(currentUsername).orElse(null);
            
            report.setStatus(AccountReport.Status.APPROVED);
            report.setResolvedAt(java.time.LocalDateTime.now());
            report.setResolvedBy(admin);
            accountReportRepository.save(report);
            
            // Criar notificação de report aprovado
            notificationService.createNotification(
                Notification.NotificationType.REPORT,
                "Report Aprovado",
                "Report do utilizador '" + report.getUser().getUsername() + "' foi aprovado",
                "/admin/reports"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Report aprovado e utilizador ativado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao aprovar report");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/api/reports/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> rejectReport(@PathVariable Long id) {
        try {
            AccountReport report = accountReportRepository.findById(id).orElse(null);
            if (report == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Marcar report como rejeitado
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User admin = userRepository.findByUsername(currentUsername).orElse(null);
            
            report.setStatus(AccountReport.Status.REJECTED);
            report.setResolvedAt(java.time.LocalDateTime.now());
            report.setResolvedBy(admin);
            accountReportRepository.save(report);
            
            // Criar notificação de report rejeitado
            notificationService.createNotification(
                Notification.NotificationType.REPORT,
                "Report Rejeitado",
                "Report do utilizador '" + report.getUser().getUsername() + "' foi rejeitado",
                "/admin/reports"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Report rejeitado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao rejeitar report");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
