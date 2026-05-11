package com.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(description = "Email del usuario", example = "admin@store.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "Contraseña del usuario", example = "admin123")
        @NotBlank
        String password
) {
}
