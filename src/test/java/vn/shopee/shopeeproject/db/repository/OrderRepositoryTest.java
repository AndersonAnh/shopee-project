package vn.shopee.shopeeproject.db.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Репозиторий проверяется на настоящем Postgres.
 * Контейнер один на весь класс, схема создаётся миграциями Flyway.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Testcontainers
class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFindById() {
        Order saved = saveOrder(OrderStatus.CONFIRMED);
        UUID customerId = saved.getCustomerId();

        // сбрасываем кэш Hibernate, чтобы findById пошёл в базу, а не в память
        entityManager.flush();
        entityManager.clear();

        Order found = orderRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(found.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void findAllByOrderStatusNot_excludesCancelled() {
        Order confirmed = saveOrder(OrderStatus.CONFIRMED);
        saveOrder(OrderStatus.CANCELLED);

        entityManager.flush();
        entityManager.clear();

        Page<Order> page = orderRepository.findAllByOrderStatusNot(
                OrderStatus.CANCELLED,
                PageRequest.of(0, 20));

        assertThat(page.getContent())
                .extracting(Order::getId)
                .containsExactly(confirmed.getId());
    }

    private Order saveOrder(OrderStatus status) {
        Order order = Order.builder()
                .customerId(UUID.randomUUID())
                .orderStatus(status)
                .totalAmount(new BigDecimal("19.99"))
                .createdAt(LocalDateTime.now())
                .build();
        return orderRepository.save(order);
    }
}
