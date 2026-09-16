package vn.shopee.shopeeproject.util;

import vn.shopee.shopeeproject.db.dto.OrderItemRequest;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.entity.OrderItem;
import vn.shopee.shopeeproject.db.entity.pojo.DeliveryAddress;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public class TestUtil {
    public static OrderResponse getOrderResponse() {
        return OrderResponse.builder()
                .orderId(UUID.fromString("f9b2c8a4-1e33-47d8-9f0a-6b5d3e8c12f1"))
                .status("CONFIRMED")
                .totalAmount(BigDecimal.valueOf(199.99))
                .createdAt(LocalDateTime.now())
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .build();
    }

    public static OrderRequest getOrderRequest() {
        return OrderRequest.builder()
                .customerId(UUID.fromString("a1b2c3d4-5e6f-47a8-9b0c-1d2e3f4a5b6c"))
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                                .quantity(2)
                                .price(BigDecimal.valueOf(29.99))
                                .build(),
                        OrderItemRequest.builder()
                                .productId(UUID.fromString("66666666-7777-8888-9999-000000000000"))
                                .quantity(1)
                                .price(BigDecimal.valueOf(15.50))
                                .build()
                ))
                .deliveryAddress(
                        DeliveryAddress.builder()
                                .city("Ho Chi Minh City")
                                .street("Nguyen Hue")
                                .house("123")
                                .apartment("7")
                                .build()
                )
                .build();
    }
    public static Order getOrder() {
        Order order = Order.builder()
                .id(UUID.fromString("f9b2c8a4-1e33-47d8-9f0a-6b5d3e8c12f1"))
                .customerId(UUID.fromString("a1b2c3d4-5e6f-47a8-9b0c-1d2e3f4a5b6c"))
                .orderStatus(OrderStatus.CREATED)
                .totalAmount(BigDecimal.valueOf(75.48))
                .createdAt(LocalDateTime.now())
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .deliveryAddress(
                        DeliveryAddress.builder()
                                .city("Ho Chi Minh City")
                                .street("Nguyen Hue")
                                .house("123")
                                .apartment("45")
                                .build()
                )
                .build();

        List<OrderItem> items = List.of(
                OrderItem.builder()
                        .order(order)
                        .productId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
                        .quantity(2)
                        .price(BigDecimal.valueOf(29.99))
                        .build(),
                OrderItem.builder()
                        .order(order)
                        .productId(UUID.fromString("ffffffff-0000-1111-2222-333333333333"))
                        .quantity(1)
                        .price(BigDecimal.valueOf(15.50))
                        .build()
        );

        order.setOrderItems(items);
        return order;
    }

    public static Order getSavedOrder() {
        Order savedOrder = Order.builder()
                .id(UUID.fromString("f9b2c8a4-1e33-47d8-9f0a-6b5d3e8c12f1"))
                .customerId(UUID.fromString("a1b2c3d4-5e6f-47a8-9b0c-1d2e3f4a5b6c"))
                .orderStatus(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(75.48))
                .createdAt(LocalDateTime.now())
                .estimatedDelivery(LocalDate.now().plusDays(5))
                .deliveryAddress(
                        DeliveryAddress.builder()
                                .city("Ho Chi Minh City")
                                .street("Nguyen Hue")
                                .house("123")
                                .apartment("45")
                                .build()
                )
                .build();

        List<OrderItem> items = List.of(
                OrderItem.builder()
                        .order(savedOrder)
                        .productId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
                        .quantity(2)
                        .price(BigDecimal.valueOf(29.99))
                        .build(),
                OrderItem.builder()
                        .order(savedOrder)
                        .productId(UUID.fromString("ffffffff-0000-1111-2222-333333333333"))
                        .quantity(1)
                        .price(BigDecimal.valueOf(15.50))
                        .build()
        );

        savedOrder.setOrderItems(items);
        return savedOrder;
    }

    public static List<OrderItem> getOrderItemsForStockCheck() {
        return List.of(
                OrderItem.builder()
                        .productId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
                        .quantity(2)
                        .price(BigDecimal.valueOf(29.99))
                        .build(),
                OrderItem.builder()
                        .productId(UUID.fromString("ffffffff-0000-1111-2222-333333333333"))
                        .quantity(1)
                        .price(BigDecimal.valueOf(15.50))
                        .build()
        );
    }

    public static StockCheckResponse getAvailableStockCheckResponse() {
        return StockCheckResponse.builder()
                .productId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
                .available(true)
                .availableQuantity(10)
                .status("IN_STOCK")
                .build();
    }

    public static StockCheckResponse getReservedStockResponse() {
        return StockCheckResponse.builder()
                .productId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
                .available(true)
                .availableQuantity(8) // остаток после резерва
                .status("RESERVED")
                .build();
    }

}
