package vn.shopee.shopeeproject.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID orderId,
        String orderStatus,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDate estimatedDelivery
) {
}
