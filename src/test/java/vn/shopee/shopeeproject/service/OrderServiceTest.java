package vn.shopee.shopeeproject.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.shopee.shopeeproject.db.dto.OrderItemRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.repository.OrderRepository;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.integration.client.WarehouseClient;
import vn.shopee.shopeeproject.integration.kafka.dto.OrderConfirmedEvent;
import vn.shopee.shopeeproject.integration.kafka.producer.OrderEventProducer;
import vn.shopee.shopeeproject.mapper.OrderMapper;
import vn.shopee.shopeeproject.util.TestUtil;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper dtoMapper;

    @Mock
    private WarehouseClient warehouseClient;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private OrderValidator orderValidator;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        var orderResponse = TestUtil.getOrderResponse();
        var orderRequest = TestUtil.getOrderRequest();
        var order = TestUtil.getOrder();
        var savedOrder = TestUtil.getSavedOrder();
        var availableStockCheckResponse = TestUtil.getAvailableStockCheckResponse();
        var reservedStockResponse = TestUtil.getReservedStockResponse();

        when(dtoMapper.toOrder(orderRequest)).thenReturn(order);
        when(warehouseClient.checkStock(any(UUID.class), any(Integer.class))).thenReturn(availableStockCheckResponse);
        when(warehouseClient.reserveStock(any(UUID.class), any(Integer.class))).thenReturn(reservedStockResponse);
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(dtoMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse response = orderService.createOrder(orderRequest);
        assertNotNull(response);
        Assertions.assertEquals(OrderStatus.CONFIRMED.name(), response.status());

        verify(orderEventProducer, times(2)).sendOrderConfirmed(any(OrderConfirmedEvent.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldUpdateStatus() {
        var orderResponse = TestUtil.getOrderResponse();
        var updateStatusRequest = TestUtil.getUpdateStatusRequest();
        var order = TestUtil.getOrder();
        var savedOrder = TestUtil.getSavedOrder();
        OrderItemRequest request = TestUtil.getRequest();
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(dtoMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse response = orderService.updateStatus(request.productId(), updateStatusRequest);

        assertNotNull(response);
        Assertions.assertEquals(OrderStatus.CONFIRMED.name(), response.status());

        verify(orderValidator).canCancel(order);
        verify(dtoMapper).updateEntity(updateStatusRequest, order);
        verify(orderRepository).save(any(Order.class));
    }


}
