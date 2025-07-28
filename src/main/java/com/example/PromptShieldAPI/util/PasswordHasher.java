package com.example.PromptShieldAPI.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        String rawPassword = "2020";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(rawPassword);

        System.out.println("Password original: " + rawPassword);
        System.out.println("Password encriptada: " + hashedPassword);
    }
}
