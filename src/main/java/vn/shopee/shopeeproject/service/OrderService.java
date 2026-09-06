package vn.shopee.shopeeproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.dto.UpdateStatusRequest;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.entity.OrderItem;
import vn.shopee.shopeeproject.db.repository.OrderRepository;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.exception.InvalidOrderStatusException;
import vn.shopee.shopeeproject.exception.OrderNotFoundException;
import vn.shopee.shopeeproject.integration.client.WarehouseClient;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;
import vn.shopee.shopeeproject.integration.kafka.dto.OrderConfirmedEvent;
import vn.shopee.shopeeproject.integration.kafka.producer.OrderEventProducer;
import vn.shopee.shopeeproject.mapper.OrderMapper;
import vn.shopee.shopeeproject.util.CheckProcessStockResponseUtil;
import vn.shopee.shopeeproject.util.CheckResponseUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final int DELIVERY_DAYS = 3;

    private final OrderRepository orderRepository;
    private final OrderMapper dtoMapper;
    private final WarehouseClient warehouseClient;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        CheckResponseUtil.validateCreateOrder(request);

        Order order = dtoMapper.toOrder(request);
        assembleOrder(order);

        for (OrderItem item : order.getOrderItems()) {
            StockCheckResponse stockCheck = warehouseClient.checkStock(item.getProductId(), item.getQuantity());
            processStockResponse(stockCheck);

            StockCheckResponse reserved = warehouseClient.reserveStock(item.getProductId(), item.getQuantity());
            CheckProcessStockResponseUtil.processReserveStockResponse(reserved);
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);
        publishOrderConfirmed(saved);
        return dtoMapper.toOrderResponse(saved);
    }

    void processStockResponse(StockCheckResponse stockCheckResponse) {
        CheckProcessStockResponseUtil.processStockResponse(stockCheckResponse);
    }

    private void assembleOrder(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            item.setOrder(order);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        order.setEstimatedDelivery(LocalDate.now().plusDays(DELIVERY_DAYS));
    }

    private void publishOrderConfirmed(Order order) {
        Instant occurredAt = Instant.now();
        for (OrderItem item : order.getOrderItems()) {
            orderEventProducer.sendOrderConfirmed(OrderConfirmedEvent.builder()
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .occurredAt(occurredAt)
                    .build());
        }
        log.info("Заказ подтверждён: orderId={}, items={}", order.getId(), order.getOrderItems().size());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + id));
        return dtoMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAllByOrderStatusNot(OrderStatus.CANCELLED, pageable)
                .map(dtoMapper::toOrderResponse);
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + id));

        validateStatusTransition(order.getOrderStatus(), request.status());

        order.setOrderStatus(request.status());
        orderRepository.save(order);
        return dtoMapper.toOrderResponse(order);
    }

    @Transactional
    public void cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + id));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Заказ уже отменён: " + id);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == OrderStatus.WAITING_STOCK || current == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException(
                    String.format("Нельзя изменить статус из %s в %s", current, target));
        }
    }
}
