package vn.shopee.shopeeproject.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import vn.shopee.shopeeproject.db.dto.OrderItemRequest;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.dto.UpdateStatusRequest;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.entity.OrderItem;

import java.time.LocalDateTime;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {LocalDateTime.class})
public interface OrderMapper {

    String STATUS = "CREATED";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "orderStatus", constant = STATUS)
    @Mapping(target = "orderItems", source = "items")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "estimatedDelivery", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    Order toOrder(OrderRequest orderRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toOrderItem(OrderItemRequest orderItemRequest);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "status", source = "orderStatus")
    OrderResponse toOrderResponse(Order order);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "orderStatus", source = "status")
    @Mapping(target = "cancelledAt", expression = "java(LocalDateTime.now())")
    void updateEntity(UpdateStatusRequest request, @MappingTarget Order order);
}
