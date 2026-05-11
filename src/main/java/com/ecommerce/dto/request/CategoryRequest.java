package com.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(

        @Schema(description = "Nombre de la categoria", example = "Electronics")
        @NotBlank
        String name
) {
}
