package vn.shopee.shopeeproject.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderItemRequest(
        UUID productId,
        Integer quantity,
        BigDecimal price
) {
}