package com.ecommerce.dto.response;

public record AuthResponse(
        String token,
        String email,
        String role
) {
}
