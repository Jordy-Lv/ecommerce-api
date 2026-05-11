package com.ecommerce.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
