package vn.shopee.shopeeproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.shopee.shopeeproject.annotation.MyLog;
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

    @MyLog
    public OrderResponse createOrder(OrderRequest request) {// возвр значение с полями заходит в параметры реквест
        CheckResponseUtil.validateCreateOrder(request);// валидация входящего параметра,если какое то поля нал или остутствует- исключение

        Order order = dtoMapper.toOrder(request);// мапим с помощью дтомаппера входящий запрос и сохранем его в сущность
        assembleOrder(order);// этот метод нужен для расчета суммы заказа и рассчета срока доставки товаров

        for (OrderItem item : order.getOrderItems()) {//циклом проходим по всем товарам и
            StockCheckResponse stockCheck = warehouseClient.checkStock(item.getProductId(), item.getQuantity());// ответ из склада кладем в перем стокчек
            CheckProcessStockResponseUtil.processStockResponse(stockCheck);//валидация ответа от склада


            StockCheckResponse reserved = warehouseClient.reserveStock(item.getProductId(), item.getQuantity());// ответ из склада что товар удачно зарезервирован и кладем в переменую резервед
            CheckProcessStockResponseUtil.processReserveStockResponse(reserved);// валидация ответа из склада,что резерв прошел успешно
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);// статус заказа = ок,подтвержден!!!
        order.setOrderStatus(OrderStatus.WAITING_STOCK);// статус заказа - waiting stock
        Order saved = orderRepository.save(order);// в перем сейвед сорхраняем заказ в репозитории
        publishOrderConfirmed(saved);// отпрвка месаджа в топик конфермед топик
        return dtoMapper.toOrderResponse(saved);// сохр заказ в репо мапим в дто
    }

    public OrderResponse getOrderById(UUID id) {
        return dtoMapper.toOrderResponse(getOrderOrThrow(id));
    }

    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAllByOrderStatusNot(OrderStatus.CANCELLED, pageable)
                .map(dtoMapper::toOrderResponse);
    }

    public OrderResponse updateStatus(UUID id, UpdateStatusRequest request) {
        Order order = getOrderOrThrow(id);

        validateStatusTransition(order.getOrderStatus(), request.status());

        order.setOrderStatus(request.status());
        orderRepository.save(order);
        return dtoMapper.toOrderResponse(order);
    }

    public void cancelOrder(UUID id) {
        Order order = getOrderOrThrow(id);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Заказ уже отменён: " + id);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void assembleOrder(Order order) {//обслуживающий метод
        BigDecimal total = BigDecimal.ZERO;//создаем переменную и присваеваем значение- 0
        for (OrderItem item : order.getOrderItems()) {// с циклом проходим все заказы и берем каждый товар сохраняем в перменую итем
            item.setOrder(order);//у каждого товара присваевается заказ
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));//цена товара умножается кол-во товара и сохр в тотал
        }
        order.setTotalAmount(total);//меняется общая цена заказа происходит рассчет общей цены суммы заказа
        order.setEstimatedDelivery(LocalDate.now().plusDays(DELIVERY_DAYS));// меняется срок доставки- т е через 3 дня
    }

    private void publishOrderConfirmed(Order order) {// отправляем месадж в кафка,в параметрах=заказ
        Instant occurredAt = Instant.now();//мы задали точку отсчета времени
        for (OrderItem item : order.getOrderItems()) {//цикл по всем товарам
            orderEventProducer.sendOrderConfirmed(OrderConfirmedEvent.builder()//отправка события по подтвержденым заказам
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .occurredAt(occurredAt)
                    .build());
        }
        log.info("Заказ подтверждён: orderId={}, items={}", order.getId(), order.getOrderItems().size());
    }

    private Order getOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + id));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == OrderStatus.WAITING_STOCK || current == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException(
                    String.format("Нельзя изменить статус из %s в %s", current, target));
        }
    }
}
