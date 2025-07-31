package com.example.PromptShieldAPI.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    @Value("${activity.log.path:logs/activity.log}")
    private String logFilePath;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_LOG_ENTRIES = 1000; // Limite máximo de entradas no log
    private static final int DISPLAY_ENTRIES = 5; // Número de entradas a mostrar na interface

    /**
     * Adiciona uma nova entrada de atividade ao log
     */
    public void logActivity(String type, String title, String description, String username) {
        try {
            ensureLogDirectoryExists();
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String logEntry = String.format("[%s] %s | %s | %s | %s%n", 
                timestamp, type, title, description, username);
            
            // Adicionar entrada ao final do ficheiro
            Files.write(Paths.get(logFilePath), logEntry.getBytes(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
            // Verificar se o ficheiro não excede o limite máximo
            trimLogFileIfNeeded();
            
        } catch (IOException e) {
            System.err.println("Erro ao escrever no log de atividade: " + e.getMessage());
        }
    }

    /**
     * Obtém as últimas 5 entradas do log para exibir na interface
     */
    public List<Map<String, Object>> getRecentActivity() {
        try {
            Path logPath = Paths.get(logFilePath);
            
            if (!Files.exists(logPath)) {
                return new ArrayList<>();
            }
            
            // Ler todas as linhas do ficheiro
            List<String> lines = Files.readAllLines(logPath);
            
            // Processar as últimas 5 linhas (ou menos se não houver 5)
            List<Map<String, Object>> activities = new ArrayList<>();
            int startIndex = Math.max(0, lines.size() - DISPLAY_ENTRIES);
            
            for (int i = lines.size() - 1; i >= startIndex; i--) {
                String line = lines.get(i);
                if (!line.trim().isEmpty()) {
                    Map<String, Object> activity = parseLogEntry(line);
                    if (activity != null) {
                        activities.add(activity);
                    }
                }
            }
            
            return activities;
            
        } catch (IOException e) {
            System.err.println("Erro ao ler log de atividade: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Atualiza o log de atividade (método para compatibilidade com a API existente)
     */
    public void updateActivityLog() {
        // Este método pode ser usado para operações de manutenção do log
        trimLogFileIfNeeded();
    }

    /**
     * Garante que o diretório do log existe
     */
    private void ensureLogDirectoryExists() throws IOException {
        Path logPath = Paths.get(logFilePath);
        Path logDir = logPath.getParent();
        
        if (logDir != null && !Files.exists(logDir)) {
            Files.createDirectories(logDir);
        }
    }

    /**
     * Remove entradas antigas se o ficheiro exceder o limite máximo
     */
    private void trimLogFileIfNeeded() {
        try {
            Path logPath = Paths.get(logFilePath);
            
            if (!Files.exists(logPath)) {
                return;
            }
            
            List<String> lines = Files.readAllLines(logPath);
            
            if (lines.size() > MAX_LOG_ENTRIES) {
                // Manter apenas as últimas MAX_LOG_ENTRIES entradas
                List<String> recentLines = lines.subList(
                    lines.size() - MAX_LOG_ENTRIES, lines.size());
                
                Files.write(logPath, recentLines);
            }
            
        } catch (IOException e) {
            System.err.println("Erro ao truncar log de atividade: " + e.getMessage());
        }
    }

    /**
     * Analisa uma entrada do log e converte para um mapa
     */
    private Map<String, Object> parseLogEntry(String logEntry) {
        try {
            // Formato: [timestamp] type | title | description | username
            if (!logEntry.startsWith("[") || !logEntry.contains("]")) {
                return null;
            }
            
            // Extrair timestamp
            int endBracket = logEntry.indexOf("]");
            String timestampStr = logEntry.substring(1, endBracket);
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
            
            // Extrair resto da linha
            String rest = logEntry.substring(endBracket + 1).trim();
            String[] parts = rest.split("\\|");
            
            if (parts.length < 4) {
                return null;
            }
            
            String type = parts[0].trim();
            String title = parts[1].trim();
            String description = parts[2].trim();
            String username = parts[3].trim();
            
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", type);
            activity.put("title", title);
            activity.put("description", description);
            activity.put("timestamp", timestamp);
            activity.put("username", username);
            
            return activity;
            
        } catch (Exception e) {
            System.err.println("Erro ao analisar entrada do log: " + logEntry);
            return null;
        }
    }

    /**
     * Obtém estatísticas do log
     */
    public Map<String, Object> getLogStatistics() {
        try {
            Path logPath = Paths.get(logFilePath);
            
            if (!Files.exists(logPath)) {
                return Map.of(
                    "totalEntries", 0,
                    "fileSize", "0 KB",
                    "lastModified", null
                );
            }
            
            long fileSize = Files.size(logPath);
            LocalDateTime lastModified = LocalDateTime.ofInstant(
                Files.getLastModifiedTime(logPath).toInstant(), 
                java.time.ZoneId.systemDefault()
            );
            
            List<String> lines = Files.readAllLines(logPath);
            int totalEntries = (int) lines.stream()
                .filter(line -> !line.trim().isEmpty())
                .count();
            
            return Map.of(
                "totalEntries", totalEntries,
                "fileSize", formatFileSize(fileSize),
                "lastModified", lastModified
            );
            
        } catch (IOException e) {
            return Map.of(
                "totalEntries", 0,
                "fileSize", "0 KB",
                "lastModified", null
            );
        }
    }

    /**
     * Formata o tamanho do ficheiro
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
} 