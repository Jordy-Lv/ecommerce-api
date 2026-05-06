package com.ecommerce.enums;

import java.util.Set;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED;

    public boolean canChangeTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> Set.of(CONFIRMED, CANCELLED).contains(next);
            case CONFIRMED -> Set.of(SHIPPED, CANCELLED).contains(next);
            case SHIPPED -> next == DELIVERED;
            case DELIVERED -> next == REFUNDED;
            default -> false;
        };
    }
}
