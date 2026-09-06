package vn.shopee.shopeeproject.dto;

import lombok.Builder;
import vn.shopee.shopeeproject.db.entity.pojo.DeliveryAddress;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderRequest(
        UUID customerId,
        List<OrderItemRequest> items,
        DeliveryAddress deliveryAddress
) {
}