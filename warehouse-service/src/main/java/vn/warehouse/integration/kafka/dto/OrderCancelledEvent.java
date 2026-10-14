package vn.warehouse.integration.kafka.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderCancelledEvent(
        UUID orderId,
        String reason,
        Instant occurredAt
) {
}