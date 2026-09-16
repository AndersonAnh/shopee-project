package vn.shopee.shopeeproject.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.entity.OrderItem;
import vn.shopee.shopeeproject.db.repository.OrderRepository;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.integration.client.WarehouseClient;
import vn.shopee.shopeeproject.integration.kafka.dto.OrderConfirmedEvent;
import vn.shopee.shopeeproject.integration.kafka.producer.OrderEventProducer;
import vn.shopee.shopeeproject.mapper.OrderMapper;
import vn.shopee.shopeeproject.util.TestUtil;

import java.util.List;

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
    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        var order = TestUtil.getOrder();
        var orderResponse = TestUtil.getOrderResponse();
        var orderRequest = TestUtil.getOrderRequest();
        var savedOrder = TestUtil.getSavedOrder();
        var availableStockCheckResponse = TestUtil.getAvailableStockCheckResponse();
        var reservedStockResponse = TestUtil.getReservedStockResponse();
        List<OrderItem> items = TestUtil.getOrderItemsForStockCheck();
        OrderItem item1 = items.get(0);
        OrderItem item2 = items.get(1);


        when(dtoMapper.toOrder(orderRequest)).thenReturn(order);
        when(warehouseClient.checkStock(item1.getProductId(), item1.getQuantity())).thenReturn(availableStockCheckResponse);
        when(warehouseClient.checkStock(item2.getProductId(), item2.getQuantity())).thenReturn(availableStockCheckResponse);
        when(warehouseClient.reserveStock(item1.getProductId(), item1.getQuantity())).thenReturn(reservedStockResponse);
        when(warehouseClient.reserveStock(item2.getProductId(), item2.getQuantity())).thenReturn(reservedStockResponse);
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(dtoMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse response = orderService.createOrder(orderRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(OrderStatus.CONFIRMED.name(), response.status());

        verify(warehouseClient).checkStock(item1.getProductId(), item1.getQuantity());
        verify(warehouseClient).checkStock(item2.getProductId(), item2.getQuantity());
        verify(warehouseClient).reserveStock(item1.getProductId(), item1.getQuantity());
        verify(warehouseClient).reserveStock(item2.getProductId(), item2.getQuantity());
        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer, times(2)).sendOrderConfirmed(any(OrderConfirmedEvent.class));
        verifyNoMoreInteractions(warehouseClient, orderEventProducer);
    }

}
