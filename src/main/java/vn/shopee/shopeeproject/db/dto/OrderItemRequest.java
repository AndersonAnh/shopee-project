package vn.shopee.shopeeproject.db.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderItemRequest(
        @NotNull(message = "productId обязателен")
        UUID productId,
        @NotNull(message = "quantity обязателен")
        @Min(value = 1, message = "Количество товара должно быть больше нуля")
        Integer quantity,
        @NotNull(message = "price обязателен")
        @DecimalMin(value = "0.00", inclusive = false, message = "Цена товара должна быть больше нуля")
        BigDecimal price
) {
}
