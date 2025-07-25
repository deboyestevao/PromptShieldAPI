package com.example.PromptShieldAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RegisterRequest {

    @Schema(description = "Email", example = "test@gmail.com")
    private String email;

    @Schema(description = "Primeiro Nome", example = "João")
    private String firstName;

    @Schema(description = "Último Nome", example = "Morais")
    private String lastName;

    @Schema(description = "Password", example = "test")
    private String password;

    @Schema(description = "Confirmar Password", example = "test")
    private String confirmPassword;

}
