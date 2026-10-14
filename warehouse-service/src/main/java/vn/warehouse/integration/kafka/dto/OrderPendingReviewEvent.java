package vn.warehouse.integration.kafka.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderPendingReviewEvent(
        UUID orderId,
        UUID productId,
        Integer quantity,
        String reason,
        Instant occurredAt
) {
}