package vn.shopee.shopeeproject.integration.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StockCheckResponse(
        UUID productId,
        boolean available,
        Integer availableQuantity,
        String status
) {
}