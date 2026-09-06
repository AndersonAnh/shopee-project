package vn.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ReserveRequest(
        @NotNull UUID productId,
        @NotNull @Min(1) Integer quantity
) {
}