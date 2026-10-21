package e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * shopee-project и warehouse-service в одной Docker-сети.
 * Образы shopee-project:e2e и warehouse-service:e2e собираются заранее,
 * после изменений в jar их нужно пересобрать.
 * Запуск: mvn verify -Pe2e
 */
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullInteractionE2ETest {

    private static E2EStack stack;

    @BeforeAll
    static void startStack() {
        stack = E2EStack.start();
    }

    @AfterAll
    static void stopStack() {
        if (stack != null) {
            stack.stop();
        }
    }

    @Test
    @Order(1)
    @DisplayName("есть товар: резерв 100→97 и склад получает order.confirmed")
    void confirmedOrderReservesStock() {
        UUID productId = UUID.randomUUID();
        stack.seedProduct(productId, 100);

        ResponseEntity<String> response = stack.createOrder(productId, 3);

        assertThat(response.getStatusCode())
                .as("body: %s", response.getBody())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"status\":\"CONFIRMED\"");
        assertThat(stack.orderStatus(productId)).contains("CONFIRMED");

        E2EStack.Stock stock = stack.stock(productId);
        assertThat(stock.available()).isEqualTo(97);
        assertThat(stock.reserved()).isEqualTo(3);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(stack.warehouseLogs()).contains("[order.confirmed]"));
    }

    @Test
    @Order(2)
    @DisplayName("остатка нет: резерв не вызывается, заказ ждёт поставку")
    void outOfStockWaitsWithoutReserve() {
        UUID productId = UUID.randomUUID();
        stack.seedProduct(productId, 0);

        ResponseEntity<String> response = stack.createOrder(productId, 1);

        assertThat(response.getStatusCode())
                .as("body: %s", response.getBody())
                .isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).contains("\"status\":\"WAITING_STOCK\"");
        assertThat(stack.orderStatus(productId)).contains("WAITING_STOCK");

        E2EStack.Stock stock = stack.stock(productId);
        assertThat(stock.available()).isZero();
        assertThat(stock.reserved()).isZero();

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(stack.warehouseLogs()).contains("[order.waiting-stock]"));
    }

    @Test
    @Order(3)
    @DisplayName("склад выключен: резерва нет, заказ остаётся CREATED")
    void warehouseDownLeavesStockUntouched() {
        UUID productId = UUID.randomUUID();
        stack.seedProduct(productId, 10);
        stack.stopWarehouse();

        ResponseEntity<String> response = stack.createOrder(productId, 1);

        assertThat(response.getStatusCode())
                .as("body: %s", response.getBody())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"status\":\"CREATED\"");
        assertThat(stack.orderStatus(productId)).contains("CREATED");
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(stack.shopeeLogs()).contains("Склад недоступен на checkStock"));

        E2EStack.Stock stock = stack.stock(productId);
        assertThat(stock.available()).isEqualTo(10);
        assertThat(stock.reserved()).isZero();
    }
}
