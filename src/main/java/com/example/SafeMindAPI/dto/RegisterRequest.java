package com.example.SafeMindAPI.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RegisterRequest {

    @Schema(description = "Username", example = "test")
    private String username;

    @Schema(description = "Password", example = "test")
    private String password;

    @Schema(description = "Role", example = "USER")
    private String role;
}
