package vn.shopee.shopeeproject.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import vn.shopee.shopeeproject.db.dto.OrderItemRequest;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {LocalDate.class, LocalDateTime.class})
public interface OrderMapper {

    String STATUS = "CREATED";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "orderStatus", constant = STATUS)
    @Mapping(target = "orderItems", source = "items")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "estimatedDelivery", ignore = true)
    Order toOrder(OrderRequest orderRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toOrderItem(OrderItemRequest orderItemRequest);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "status", source = "orderStatus")
    OrderResponse toOrderResponse(Order order);

    @AfterMapping
    default void afterToOrder(OrderRequest request, @MappingTarget Order order) {
        List<OrderItem> items = order.getOrderItems();
        if (items != null) {
            items.forEach(item -> item.setOrder(order));
        }
        order.setTotalAmount(calculateTotal(items));
        order.setEstimatedDelivery(LocalDate.now().plusDays(3));
    }

    private static BigDecimal calculateTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
