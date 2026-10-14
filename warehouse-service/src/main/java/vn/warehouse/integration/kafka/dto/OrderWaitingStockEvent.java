package vn.warehouse.integration.kafka.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderWaitingStockEvent(
        UUID orderId,
        UUID productId,
        Integer requestedQuantity,
        Integer availableQuantity,
        Instant occurredAt
) {
}