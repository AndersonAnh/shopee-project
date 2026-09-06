package vn.shopee.shopeeproject.db.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import vn.shopee.shopeeproject.enums.OrderStatus;

@Builder
public record UpdateStatusRequest(
        @NotNull(message = "статус обязателен")
        OrderStatus status
) {}