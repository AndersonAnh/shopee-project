package vn.shopee.shopeeproject.integration.client;

import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import io.github.resilience4j.springboot3.spelresolver.autoconfigure.SpelResolverConfigurationOnMissingBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import vn.shopee.shopeeproject.config.retry.IntegrationRetryProperties;
import vn.shopee.shopeeproject.config.retry.RetryConfig;
import vn.shopee.shopeeproject.config.warehouserestconfig.WarehouseConfig;
import vn.shopee.shopeeproject.exception.WarehouseUnavailableException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(
        classes = WarehouseClientResilienceTest.TestConfig.class,
        properties = {
                "integration.warehouse.url=http://warehouse.test",
                // держим breaker закрытым, чтобы проверять именно исчерпание retry
                "resilience4j.circuitbreaker.instances.warehouse.minimum-number-of-calls=100",
                "integration.retry.config.max-attempts=3",
                "integration.retry.config.initial-interval=10",
                "integration.retry.config.multiplier=1.0",
                "integration.retry.config.max-interval=10",
                "integration.retry.config.retryable-exceptions[0]=org.springframework.web.client.ResourceAccessException",
                "integration.retry.config.retryable-exceptions[1]=org.springframework.web.client.HttpServerErrorException"
        })
class WarehouseClientResilienceTest {

    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String CHECK_URL =
            "http://warehouse.test/api/warehouse/check?productId=" + PRODUCT_ID + "&qty=1";

    @Autowired
    private WarehouseClient warehouseClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void checkStock_whenWarehouseReturns5xx_retriesThenFailsAsUnavailable() {
        server.expect(times(3), requestTo(CHECK_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> warehouseClient.checkStock(PRODUCT_ID, 1))
                .isInstanceOf(WarehouseUnavailableException.class);

        server.verify();
    }

    @Test
    void checkStock_whenWarehouseReturns4xx_propagatesClientError() {
        server.expect(times(1), requestTo(CHECK_URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> warehouseClient.checkStock(PRODUCT_ID, 1))
                .isInstanceOf(HttpClientErrorException.class);

        server.verify();
    }

    @Configuration
    @Import({WarehouseClient.class, RetryConfig.class, IntegrationRetryProperties.class})
    @ImportAutoConfiguration({
            AopAutoConfiguration.class,
            ConfigurationPropertiesAutoConfiguration.class,
            SpelResolverConfigurationOnMissingBean.class,
            FallbackConfigurationOnMissingBean.class,
            CircuitBreakerAutoConfiguration.class
    })
    static class TestConfig {

        @Bean("warehouseRestTemplate")
        RestTemplate warehouseRestTemplate() {
            return new RestTemplate();
        }

        @Bean
        WarehouseConfig warehouseConfig(
                @org.springframework.beans.factory.annotation.Value("${integration.warehouse.url}") String url) {
            WarehouseConfig config = new WarehouseConfig();
            config.setUrl(url);
            return config;
        }
    }
}
