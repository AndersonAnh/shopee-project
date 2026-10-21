package e2e;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Один прогон: сеть, две базы, Kafka и оба сервиса.
 * Тест остаётся сценарием, адреса и SQL живут здесь.
 */
final class E2EStack {

    private static final DockerImageName KAFKA = DockerImageName.parse("apache/kafka:3.7.1");
    private static final String CLUSTER_ID = "MkU3OEVBNTcwNTJENDM2Qk";

    private final Network network;
    private final PostgreSQLContainer<?> shopeeDb;
    private final PostgreSQLContainer<?> warehouseDb;
    private final GenericContainer<?> kafka;
    private final GenericContainer<?> warehouseService;
    private final GenericContainer<?> shopeeService;
    private final RestTemplate http = new RestTemplate();

    private E2EStack(Network network,
                     PostgreSQLContainer<?> shopeeDb,
                     PostgreSQLContainer<?> warehouseDb,
                     GenericContainer<?> kafka,
                     GenericContainer<?> warehouseService,
                     GenericContainer<?> shopeeService) {
        this.network = network;
        this.shopeeDb = shopeeDb;
        this.warehouseDb = warehouseDb;
        this.kafka = kafka;
        this.warehouseService = warehouseService;
        this.shopeeService = shopeeService;
    }

    static E2EStack start() {
        Network network = Network.newNetwork();
        PostgreSQLContainer<?> shopeeDb = null;
        PostgreSQLContainer<?> warehouseDb = null;
        GenericContainer<?> kafka = null;
        GenericContainer<?> warehouseService = null;
        GenericContainer<?> shopeeService = null;
        try {
            shopeeDb = postgres("shopee-project", "admin", "shopee", "shopee-db", network);
            shopeeDb.start();

            warehouseDb = postgres("warehouse_service_db", "admin", "warehouse", "warehouse-db", network);
            warehouseDb.start();

            kafka = kafka(network);
            kafka.start();

            warehouseService = warehouse(network);
            warehouseService.start();

            shopeeService = shopee(network);
            shopeeService.start();

            return new E2EStack(network, shopeeDb, warehouseDb, kafka, warehouseService, shopeeService);
        } catch (RuntimeException exception) {
            stopQuietly(shopeeService, warehouseService, kafka, warehouseDb, shopeeDb);
            network.close();
            throw exception;
        }
    }

    void stop() {
        stopQuietly(shopeeService, warehouseService, kafka, warehouseDb, shopeeDb);
        network.close();
    }

    void stopWarehouse() {
        warehouseService.stop();
    }

    String warehouseLogs() {
        return warehouseService.getLogs();
    }

    String shopeeLogs() {
        return shopeeService.getLogs();
    }

    ResponseEntity<String> createOrder(UUID productId, int quantity) {
        String body = """
                {
                  "customerId": "%s",
                  "items": [
                    {"productId": "%s", "quantity": %d, "price": 100.00}
                  ],
                  "deliveryAddress": {
                    "city": "Moscow", "street": "Tverskaya", "house": "1", "apartment": "10"
                  }
                }
                """.formatted(UUID.randomUUID(), productId, quantity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://" + shopeeService.getHost() + ":" + shopeeService.getMappedPort(8080) + "/api/orders";
        try {
            return http.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        } catch (HttpStatusCodeException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getResponseBodyAsString());
        }
    }

    void seedProduct(UUID productId, int available) {
        String sql = "INSERT INTO product_stock (product_id, available_quantity, reserved_quantity) VALUES (?, ?, 0)";
        try (Connection connection = warehouseConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, productId);
            statement.setInt(2, available);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось записать остаток " + productId, exception);
        }
    }

    Stock stock(UUID productId) {
        String sql = "SELECT available_quantity, reserved_quantity FROM product_stock WHERE product_id = ?";
        try (Connection connection = warehouseConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, productId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalStateException("Товар " + productId + " не найден в базе склада");
                }
                return new Stock(rows.getInt("available_quantity"), rows.getInt("reserved_quantity"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось прочитать остаток " + productId, exception);
        }
    }

    Optional<String> orderStatus(UUID productId) {
        String sql = """
                SELECT o.order_status
                FROM orders o
                JOIN order_items i ON i.order_id = o.id
                WHERE i.product_id = ?
                """;
        try (Connection connection = shopeeConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, productId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                return Optional.of(rows.getString("order_status"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Не удалось прочитать заказ по товару " + productId, exception);
        }
    }

    private Connection warehouseConnection() throws SQLException {
        return DriverManager.getConnection(
                warehouseDb.getJdbcUrl(), warehouseDb.getUsername(), warehouseDb.getPassword());
    }

    private Connection shopeeConnection() throws SQLException {
        return DriverManager.getConnection(
                shopeeDb.getJdbcUrl(), shopeeDb.getUsername(), shopeeDb.getPassword());
    }

    private static PostgreSQLContainer<?> postgres(String database, String username, String password,
                                                   String alias, Network network) {
        return new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName(database)
                .withUsername(username)
                .withPassword(password)
                .withNetwork(network)
                .withNetworkAliases(alias);
    }

    private static GenericContainer<?> kafka(Network network) {
        return new GenericContainer<>(KAFKA)
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withExposedPorts(9092)
                .withEnv("CLUSTER_ID", CLUSTER_ID)
                .withEnv("KAFKA_NODE_ID", "1")
                .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
                .withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093")
                .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092")
                .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT")
                .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
                .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
                .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@kafka:9093")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                .waitingFor(Wait.forLogMessage(".*Kafka Server started.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(2)));
    }

    private static GenericContainer<?> warehouse(Network network) {
        return new GenericContainer<>(DockerImageName.parse("warehouse-service:e2e"))
                .withNetwork(network)
                .withNetworkAliases("warehouse")
                .withExposedPorts(8081, 8098)
                .withEnv("JAVA_TOOL_OPTIONS", String.join(" ",
                        "-Dspring.datasource.url=jdbc:postgresql://warehouse-db:5432/warehouse_service_db",
                        "-Dspring.datasource.username=admin",
                        "-Dspring.datasource.password=warehouse",
                        "-Dspring.kafka.bootstrap-servers=kafka:9092",
                        "-Dserver.port=8081",
                        "-Dspring.flyway.enabled=true"))
                .waitingFor(Wait.forHttp("/actuator/health")
                        .forPort(8098)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));
    }

    private static GenericContainer<?> shopee(Network network) {
        return new GenericContainer<>(DockerImageName.parse("shopee-project:e2e"))
                .withNetwork(network)
                .withNetworkAliases("shopee")
                .withExposedPorts(8080, 8088)
                .withEnv("JAVA_TOOL_OPTIONS", String.join(" ",
                        "-Dspring.datasource.url=jdbc:postgresql://shopee-db:5432/shopee-project",
                        "-Dspring.datasource.username=admin",
                        "-Dspring.datasource.password=shopee",
                        "-Dspring.kafka.bootstrap-servers=kafka:9092",
                        "-Dintegration.warehouse.url=http://warehouse:8081",
                        "-Dserver.port=8080",
                        "-Dspring.flyway.enabled=true",
                        "-Dintegration.retry.config.initialInterval=100",
                        "-Dintegration.retry.config.maxInterval=200",
                        "-Dintegration.retry.config.multiplier=1.0"))
                .waitingFor(Wait.forHttp("/actuator/health")
                        .forPort(8088)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));
    }

    private static void stopQuietly(GenericContainer<?> shopeeService,
                                    GenericContainer<?> warehouseService,
                                    GenericContainer<?> kafka,
                                    PostgreSQLContainer<?> warehouseDb,
                                    PostgreSQLContainer<?> shopeeDb) {
        stopOne(shopeeService);
        stopOne(warehouseService);
        stopOne(kafka);
        stopOne(warehouseDb);
        stopOne(shopeeDb);
    }

    private static void stopOne(org.testcontainers.lifecycle.Startable container) {
        if (container != null) {
            container.stop();
        }
    }

    record Stock(int available, int reserved) {
    }
}
