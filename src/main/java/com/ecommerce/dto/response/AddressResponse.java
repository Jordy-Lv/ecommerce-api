package com.ecommerce.dto.response;

public record AddressResponse(
        Long id,
        String street,
        String city,
        String country,
        String zipCode
) {
}
