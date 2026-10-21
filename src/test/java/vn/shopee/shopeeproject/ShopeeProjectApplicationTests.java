package vn.shopee.shopeeproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class ShopeeProjectApplicationTests {

    // @ServiceConnection сам пробрасывает spring.datasource.url/username/password
    // в контекст — руками через @DynamicPropertySource прописывать не нужно
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void contextLoads() {
        // проверяем, что контекст поднимается с реальным Postgres в docker,
        // без обращения к какой-либо локальной БД
    }
}