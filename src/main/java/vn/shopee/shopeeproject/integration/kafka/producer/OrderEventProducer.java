package vn.shopee.shopeeproject.integration.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.shopee.shopeeproject.integration.kafka.dto.*;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC_ORDER_CONFIRMED = "order.confirmed";
    private static final String TOPIC_ORDER_WAITING_STOCK = "order.waiting-stock";
    private static final String TOPIC_ORDER_CANCELLED = "order.cancelled";
    private static final String TOPIC_ORDER_PENDING_REVIEW = "order.pending-review";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderConfirmed(OrderConfirmedEvent event) {
        send(TOPIC_ORDER_CONFIRMED, event.orderId().toString(), event);
    }

    public void sendOrderWaitingStock(OrderWaitingStockEvent event) {
        send(TOPIC_ORDER_WAITING_STOCK, event.orderId().toString(), event);
    }

    public void sendOrderCancelled(OrderCancelledEvent event) {
        send(TOPIC_ORDER_CANCELLED, event.orderId().toString(), event);
    }

    public void sendOrderPendingReview(OrderPendingReviewEvent event) {
        send(TOPIC_ORDER_PENDING_REVIEW, event.orderId().toString(), event);
    }

    private void send(String topic, String key, Object event) {
        try {
            // 1. Сериализация через ObjectMapper
            String jsonPayload = objectMapper.writeValueAsString(event);

            // 2. ЯВНО создаём ProducerRecord (по ТЗ!)
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    key,
                    jsonPayload
            );

            kafkaTemplate.send(record).get(5, TimeUnit.SECONDS);
            log.info("Успешно отправлено: topic={}, key={}", topic, key);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации события для топика {}", topic, e);
            throw new IllegalStateException("Не удалось сериализовать событие Kafka", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Отправка в Kafka прервана: topic=" + topic, e);
        } catch (Exception e) {
            log.error("Ошибка отправки в Kafka: topic={}, key={}", topic, key, e);
            throw new IllegalStateException("Не удалось отправить событие в Kafka: topic=" + topic, e);
        }
    }
}