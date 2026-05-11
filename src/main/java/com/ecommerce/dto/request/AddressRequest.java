package com.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(

        @Schema(description = "Calle y numero", example = "Av. Siempre Viva 742")
        @NotBlank
        String street,

        @Schema(description = "Ciudad", example = "Lima")
        @NotBlank
        String city,

        @Schema(description = "Pais", example = "Peru")
        @NotBlank
        String country,

        @Schema(description = "Codigo postal", example = "15001")
        @NotBlank
        String zipCode
) {
}
