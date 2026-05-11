package com.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(

        @Schema(description = "Id del producto a comprar", example = "1")
        @NotNull
        Long productId,

        @Schema(description = "Cantidad de unidades", example = "2")
        @Min(1)
        int quantity
) {
}
