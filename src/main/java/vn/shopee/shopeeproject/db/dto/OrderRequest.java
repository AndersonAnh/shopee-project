package vn.shopee.shopeeproject.db.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import vn.shopee.shopeeproject.db.entity.pojo.DeliveryAddress;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderRequest(
        @NotNull(message = "customerId обязателен")
        UUID customerId,
        @NotEmpty(message = "Заказ должен содержать хотя бы одну позицию")
        @Valid
        List<OrderItemRequest> items,
        @NotNull(message = "Адрес доставки обязателен")
        @Valid
        DeliveryAddress deliveryAddress
) {
}
