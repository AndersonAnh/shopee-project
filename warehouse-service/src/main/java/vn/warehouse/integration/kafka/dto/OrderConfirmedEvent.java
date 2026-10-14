package vn.warehouse.integration.kafka.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderConfirmedEvent(
        UUID orderId,
        UUID productId,
        Integer quantity,
        Instant occurredAt
) {
}
