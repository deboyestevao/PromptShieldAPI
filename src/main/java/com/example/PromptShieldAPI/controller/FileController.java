package com.example.PromptShieldAPI.controller;

import com.example.PromptShieldAPI.model.User;
import com.example.PromptShieldAPI.repository.UserRepository;
import com.example.PromptShieldAPI.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<String> fileIds = fileService.saveFiles(files, username);
        
        // Atualizar lastActive do usuário
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setLastActive(java.time.LocalDateTime.now());
            userRepository.save(user);
        }
        
        return ResponseEntity.ok(fileIds);
    }
}
