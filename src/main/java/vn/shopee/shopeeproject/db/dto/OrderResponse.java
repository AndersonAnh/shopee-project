package vn.shopee.shopeeproject.db.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID orderId,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDate estimatedDelivery
) {
}
