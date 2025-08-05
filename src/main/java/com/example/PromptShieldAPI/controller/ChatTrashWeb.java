package com.example.PromptShieldAPI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatTrashWeb {

    @GetMapping("/chat/trash")
    public String chatTrashPage() {
        return "chatTrash";
    }
} 