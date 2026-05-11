package com.ecommerce.dto.request;

import com.ecommerce.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

        @Schema(description = "Nuevo estado de la orden", example = "CONFIRMED")
        @NotNull
        OrderStatus status
) {
}
