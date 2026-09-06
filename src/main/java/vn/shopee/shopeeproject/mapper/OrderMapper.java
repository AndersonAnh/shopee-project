package vn.shopee.shopeeproject.mapper;

import org.mapstruct.*;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.entity.OrderItem;
import vn.shopee.shopeeproject.db.dto.OrderItemRequest;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {ZonedDateTime.class, LocalDate.class, LocalDateTime.class})
public interface DtoMapper {

 String STATUS = "CREATED";

 @Mapping(target = "id", ignore = true)
 @Mapping(target = "customerId", source = "customerId")
 @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
 @Mapping(target = "orderStatus", constant = "CREATED")
 @Mapping(target = "deliveryAddress", source = "deliveryAddress")
 @Mapping(target = "orderItems", source = "items")
 @Mapping(target = "totalAmount", ignore = true)
 @Mapping(target = "estimatedDelivery", ignore = true)
 Order toOrder(OrderRequest orderRequest);

 OrderItem toOrderItem(OrderItemRequest orderItemRequest);

 @Mapping(target = "orderId", source = "id")
 @Mapping(target = "status", source = "orderStatus")
 OrderResponse toOrderResponse(Order order);

 // Метод для расчета суммы заказа после маппинга
 @AfterMapping
 default void calculateTotalAmount(OrderRequest request, @MappingTarget Order order) {
  if (order.getOrderItems() != null) {
   double total = order.getOrderItems().stream()
           .mapToDouble(item -> item.getPrice() * item.getQuantity())
           .sum();
   order.setTotalAmount(total);
  }
 }
}