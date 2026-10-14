package vn.shopee.shopeeproject.integration.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ReserveRequest(
        UUID productId,
        Integer quantity
) {
}
