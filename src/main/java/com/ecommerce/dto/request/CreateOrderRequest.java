package com.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(

        @Schema(description = "Lista de items de la orden")
        @NotEmpty
        @Valid
        List<OrderItemRequest> items,

        @Schema(description = "Id de la direccion de envio", example = "1")
        @NotNull
        Long addressId
) {
}
