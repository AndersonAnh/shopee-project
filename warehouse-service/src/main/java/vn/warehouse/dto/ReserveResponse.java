package vn.warehouse.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ReserveResponse(
        UUID productId,
        boolean success,
        String message
) {}