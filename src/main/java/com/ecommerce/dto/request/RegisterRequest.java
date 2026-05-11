package com.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Schema(description = "Nombre completo del usuario", example = "Juan Perez")
        @NotBlank
        String name,

        @Schema(description = "Email del usuario, debe ser unico", example = "juan@store.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "Contraseña del usuario", example = "miPassword123")
        @NotBlank
        @Size(min = 6)
        String password
) {
}
