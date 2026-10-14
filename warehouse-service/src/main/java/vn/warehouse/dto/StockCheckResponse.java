package vn.warehouse.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StockCheckResponse(
        UUID productId,
        boolean available,
        Integer availableQuantity,
        String status
) {}