package vn.warehouse.integration.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.warehouse.integration.kafka.dto.OrderCancelledEvent;
import vn.warehouse.integration.kafka.dto.OrderConfirmedEvent;
import vn.warehouse.integration.kafka.dto.OrderPendingReviewEvent;
import vn.warehouse.integration.kafka.dto.OrderWaitingStockEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.confirmed", groupId = "warehouse-service")
    public void handleOrderConfirmed(ConsumerRecord<String, String> record) {
        log.info("Получено событие [order.confirmed]: partition={}, offset={}, key={}",
                record.partition(), record.offset(), record.key());

        try {
            OrderConfirmedEvent event = objectMapper.readValue(record.value(), OrderConfirmedEvent.class);
            log.info("Десериализовано: orderId={}, {}", event.orderId(), event);

            // бизнес-логика warehouse
            processConfirmed(event);

        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации order.confirmed: value={}", record.value(), e);
        }
    }

    @KafkaListener(topics = "order.waiting-stock", groupId = "warehouse-service")
    public void handleOrderWaitingStock(ConsumerRecord<String, String> record) {
        log.info("Получено событие [order.waiting-stock]: partition={}, offset={}, key={}",
                record.partition(), record.offset(), record.key());

        try {
            OrderWaitingStockEvent event = objectMapper.readValue(record.value(), OrderWaitingStockEvent.class);
            log.info("Десериализовано: orderId={}, {}", event.orderId(), event);

            processWaitingStock(event);

        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации order.waiting-stock: value={}", record.value(), e);
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "warehouse-service")
    public void handleOrderCancelled(ConsumerRecord<String, String> record) {
        log.info("Получено событие [order.cancelled]: partition={}, offset={}, key={}",
                record.partition(), record.offset(), record.key());

        try {
            OrderCancelledEvent event = objectMapper.readValue(record.value(), OrderCancelledEvent.class);
            log.info("Десериализовано: orderId={}, {}", event.orderId(), event);

            processCancelled(event);

        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации order.cancelled: value={}", record.value(), e);
        }
    }

    @KafkaListener(topics = "order.pending-review", groupId = "warehouse-service")
    public void handleOrderPendingReview(ConsumerRecord<String, String> record) {
        log.info("Получено событие [order.pending-review]: partition={}, offset={}, key={}",
                record.partition(), record.offset(), record.key());

        try {
            OrderPendingReviewEvent event = objectMapper.readValue(record.value(), OrderPendingReviewEvent.class);
            log.info("Десериализовано: orderId={}, {}", event.orderId(), event);

            processPendingReview(event);

        } catch (JsonProcessingException e) {
            log.error("Ошибка десериализации order.pending-review: value={}", record.value(), e);
        }
    }

    // ---------- бизнес-методы ----------

    private void processConfirmed(OrderConfirmedEvent event) {
        log.info("WAREHOUSE: резервируем товар для заказа {}", event.orderId());
        // ...
    }

    private void processWaitingStock(OrderWaitingStockEvent event) {
        log.info("WAREHOUSE: проверяем остатки для заказа {}", event.orderId());
        // ...
    }

    private void processCancelled(OrderCancelledEvent event) {
        log.info("WAREHOUSE: возвращаем товар на склад по заказу {}", event.orderId());
        // ...
    }

    private void processPendingReview(OrderPendingReviewEvent event) {
        log.info("WAREHOUSE: подготовка отгрузки для заказа {}", event.orderId());
        // ...
    }
}