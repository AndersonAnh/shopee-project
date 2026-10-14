package vn.shopee.shopeeproject.integration.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ReleaseCheckRequest(
        UUID productId,
        Integer quantity
) {
}
