package com.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(

        @Schema(description = "Nombre del producto", example = "Laptop Dell")
        @NotBlank
        String name,

        @Schema(description = "Descripcion del producto", example = "Laptop 15 pulgadas 16GB RAM")
        @NotBlank
        String description,

        @Schema(description = "Precio del producto", example = "899.99")
        @NotNull
        @DecimalMin("0.01")
        BigDecimal price,

        @Schema(description = "Cantidad en stock", example = "10")
        @Min(0)
        int stock,

        @Schema(description = "Id de la categoria", example = "1")
        @NotNull
        Long categoryId
) {
}
