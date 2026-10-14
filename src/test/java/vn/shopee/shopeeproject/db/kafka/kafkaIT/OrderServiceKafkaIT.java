package vn.shopee.shopeeproject.db.kafka.kafkaIT;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import vn.shopee.shopeeproject.db.dto.OrderItemRequest;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.db.entity.pojo.DeliveryAddress;
import vn.shopee.shopeeproject.db.repository.OrderRepository;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.integration.client.WarehouseClient;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;
import vn.shopee.shopeeproject.service.OrderService;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class OrderServiceKafkaIT {

    // ---------- Инфраструктура ----------

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.4"));

    // ---------- То, что тестируем ----------

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        // Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Kafka — вот здесь ключевая строка
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }



    @Autowired
    OrderService orderService;
    @Autowired OrderRepository orderRepository;

    // ---------- Мок склада ----------

    @MockBean WarehouseClient warehouseClient;

    // ---------- Топики создаём явно ----------

    @TestConfiguration
    static class TopicsConfig {
        @Bean NewTopic orderConfirmed()     { return new NewTopic("order.confirmed", 1, (short) 1); }
        @Bean NewTopic orderWaitingStock()  { return new NewTopic("order.waiting-stock", 1, (short) 1); }
        @Bean NewTopic orderCancelled()     { return new NewTopic("order.cancelled", 1, (short) 1); }
        @Bean NewTopic orderPendingReview() { return new NewTopic("order.pending-review", 1, (short) 1); }
    }

    // ---------- Тест ----------

    @Test
    void createOrder_savesToDb_and_publishesConfirmedEvent() {
        // given: склад отвечает «товар есть и зарезервирован»
        when(warehouseClient.checkStock(any(UUID.class), anyInt()))
                .thenReturn(StockCheckResponse.builder()
                        .available(true)
                        .availableQuantity(100)
                        .status("OK")
                        .build());
        when(warehouseClient.reserveStock(any(UUID.class), anyInt()))
                .thenReturn(StockCheckResponse.builder()
                        .available(true)
                        .availableQuantity(100)
                        .status("RESERVED")
                        .build());

        UUID productId = UUID.randomUUID();
        OrderRequest request = OrderRequest.builder()
                .customerId(UUID.randomUUID())
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId(productId)
                                .quantity(2)
                                .price(new BigDecimal("9.99"))
                                .build()
                ))
                .deliveryAddress(DeliveryAddress.builder()
                        .city("Hanoi")
                        .street("Nguyen Trai")
                        .house("42")
                        .apartment("7A")
                        .build())
                .build();

        // when
        OrderResponse response = orderService.createOrder(request);

        // then 1 — заказ в БД со статусом CONFIRMED
        Order savedOrder = orderRepository.findById(response.orderId()).orElseThrow();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("19.98");

        // then 2 — событие в order.confirmed
        List<ConsumerRecord<String, String>> records = readAll("order.confirmed", Duration.ofSeconds(10));
        assertThat(records)
                .as("На один item — одно событие")
                .hasSize(1);

        ConsumerRecord<String, String> record = records.get(0);
        assertThat(record.key()).isEqualTo(savedOrder.getId().toString());
        assertThat(record.value())
                .contains("\"orderId\":\"" + savedOrder.getId() + "\"")
                .contains("\"productId\":\"" + productId + "\"")
                .contains("\"quantity\":2");
    }

    // ---------- Helpers ----------

    private List<ConsumerRecord<String, String>> readAll(String topic, Duration timeout) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("group.id", "test-" + UUID.randomUUID()); // уникальная группа — иначе offset протечёт
        props.put("auto.offset.reset", "earliest");         // читать с начала топика
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(topic));
            var records = consumer.poll(timeout);
            var result = new ArrayList<ConsumerRecord<String, String>>();
            records.forEach(result::add);
            return result;
        }
    }
}