package vn.shopee.shopeeproject.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.shopee.shopeeproject.db.dto.OrderItemRequest;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.entity.OrderItem;
import vn.shopee.shopeeproject.db.entity.pojo.DeliveryAddress;
import vn.shopee.shopeeproject.db.repository.OrderRepository;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.integration.client.WarehouseClient;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;
import vn.shopee.shopeeproject.integration.kafka.dto.OrderConfirmedEvent;
import vn.shopee.shopeeproject.integration.kafka.producer.OrderEventProducer;
import vn.shopee.shopeeproject.mapper.OrderMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceCreateOrderTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper dtoMapper;
    @Mock
    private WarehouseClient warehouseClient;
    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_whenStockAvailable_confirmsPersistsAndPublishes() {
        UUID productId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID customerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID orderId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        OrderRequest request = OrderRequest.builder()
                .customerId(customerId)
                .items(List.of(OrderItemRequest.builder()
                        .productId(productId)
                        .quantity(2)
                        .price(new BigDecimal("199.99"))
                        .build()))
                .deliveryAddress(DeliveryAddress.builder()
                        .city("Москва")
                        .street("Ленина")
                        .house("10")
                        .apartment("25")
                        .build())
                .build();

        OrderItem item = OrderItem.builder()
                .productId(productId)
                .quantity(2)
                .price(new BigDecimal("199.99"))
                .build();
        Order order = Order.builder()
                .customerId(customerId)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .orderItems(List.of(item))
                .build();

        StockCheckResponse available = StockCheckResponse.builder()
                .productId(productId)
                .available(true)
                .availableQuantity(10)
                .status("OK")
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status("CONFIRMED")
                .totalAmount(new BigDecimal("399.98"))
                .createdAt(order.getCreatedAt())
                .estimatedDelivery(order.getEstimatedDelivery())
                .build();

        when(dtoMapper.toOrder(request)).thenReturn(order);
        when(warehouseClient.checkStock(productId, 2)).thenReturn(available);
        when(warehouseClient.reserveStock(productId, 2)).thenReturn(available);
        when(orderRepository.save(order)).thenAnswer(invocation -> {
            order.setId(orderId);
            return order;
        });
        when(dtoMapper.toOrderResponse(order)).thenReturn(response);

        OrderResponse result = orderService.createOrder(request);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.status()).isEqualTo("CONFIRMED");
        assertThat(result.orderId()).isEqualTo(orderId);

        // сборку агрегата делает сервис: обратная ссылка, сумма и дата доставки
        assertThat(item.getOrder()).isSameAs(order);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("399.98");
        assertThat(order.getEstimatedDelivery()).isEqualTo(LocalDate.now().plusDays(3));

        verify(warehouseClient).checkStock(productId, 2);
        verify(warehouseClient).reserveStock(productId, 2);
        verify(orderRepository).save(order);

        ArgumentCaptor<OrderConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
        verify(orderEventProducer).sendOrderConfirmed(eventCaptor.capture());
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(orderId);
        assertThat(eventCaptor.getValue().productId()).isEqualTo(productId);
        assertThat(eventCaptor.getValue().quantity()).isEqualTo(2);
        verify(dtoMapper).toOrderResponse(any(Order.class));
    }
}
